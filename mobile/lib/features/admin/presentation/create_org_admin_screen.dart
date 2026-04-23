import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/features/admin/data/user_service.dart';
import 'package:mobile/shared/widgets/app_text_field.dart';
import 'package:mobile/shared/widgets/primary_button.dart';

class CreateOrgAdminScreen extends ConsumerStatefulWidget {
  const CreateOrgAdminScreen({super.key});

  @override
  ConsumerState<CreateOrgAdminScreen> createState() => _CreateOrgAdminScreenState();
}

class _CreateOrgAdminScreenState extends ConsumerState<CreateOrgAdminScreen> {
  final _email = TextEditingController();
  final _password = TextEditingController();
  final _firstName = TextEditingController();
  final _lastName = TextEditingController();
  final _phone = TextEditingController();
  bool _loading = false;

  @override
  void dispose() {
    _email.dispose();
    _password.dispose();
    _firstName.dispose();
    _lastName.dispose();
    _phone.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final email = _email.text.trim();
    final password = _password.text;
    final firstName = _firstName.text.trim();
    final lastName = _lastName.text.trim();
    final phone = _phone.text.trim().isEmpty ? null : _phone.text.trim();

    String? error;
    if (firstName.isEmpty || lastName.isEmpty) error = 'Name is required';
    final emailRegex = RegExp(r'^[^@\s]+@[^@\s]+\.[^@\s]+$');
    if (error == null && !emailRegex.hasMatch(email)) error = 'Invalid email';
    if (error == null && password.length < 8) error = 'Password must be 8+ chars';

    if (error != null) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(error), backgroundColor: Colors.red));
      return;
    }

    setState(() => _loading = true);
    try {
      final token = ref.read(authControllerProvider).accessToken;
      if (token == null) throw Exception('Not authenticated');

      final userService = ref.read(userServiceProvider);
      final res = await userService.provisionUser(
        token,
        email: email,
        password: password,
        firstName: firstName,
        lastName: lastName,
        role: 'ADMIN_ORG',
        phone: phone,
      );

      if (!res.success) {
        throw Exception(res.error?.message ?? 'Failed to provision user');
      }

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Admin provisioned successfully')));
      context.pop();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(e.toString()), backgroundColor: Colors.red));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Provision Org Admin')),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            children: [
              AppTextField(controller: _firstName, label: 'First Name'),
              const SizedBox(height: 12),
              AppTextField(controller: _lastName, label: 'Last Name'),
              const SizedBox(height: 12),
              AppTextField(controller: _email, label: 'Email', keyboardType: TextInputType.emailAddress),
              const SizedBox(height: 12),
              AppTextField(controller: _phone, label: 'Phone (optional)', keyboardType: TextInputType.phone),
              const SizedBox(height: 12),
              TextField(
                controller: _password,
                obscureText: true,
                decoration: const InputDecoration(labelText: 'Password', border: OutlineInputBorder()),
              ),
              const SizedBox(height: 24),
              PrimaryButton(label: 'Provision Admin', onPressed: _submit, isLoading: _loading),
            ],
          ),
        ),
      ),
    );
  }
}
