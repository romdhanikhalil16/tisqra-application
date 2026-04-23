import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/features/admin/data/user_service.dart';
import 'package:mobile/shared/widgets/app_text_field.dart';
import 'package:mobile/shared/widgets/primary_button.dart';

class CreateRegularUserScreen extends ConsumerStatefulWidget {
  const CreateRegularUserScreen({super.key});

  @override
  ConsumerState<CreateRegularUserScreen> createState() => _CreateRegularUserScreenState();
}

class _CreateRegularUserScreenState extends ConsumerState<CreateRegularUserScreen> {
  final _email = TextEditingController();
  final _firstName = TextEditingController();
  final _lastName = TextEditingController();
  final _phone = TextEditingController();
  String _selectedRole = 'GUEST';
  bool _loading = false;

  @override
  void dispose() {
    _email.dispose();
    _firstName.dispose();
    _lastName.dispose();
    _phone.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final email = _email.text.trim();
    final firstName = _firstName.text.trim();
    final lastName = _lastName.text.trim();
    final phone = _phone.text.trim().isEmpty ? null : _phone.text.trim();

    String? error;
    if (firstName.isEmpty || lastName.isEmpty) error = 'Name is required';
    final emailRegex = RegExp(r'^[^@\s]+@[^@\s]+\.[^@\s]+$');
    if (error == null && !emailRegex.hasMatch(email)) error = 'Invalid email';

    if (error != null) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(error), backgroundColor: Colors.red));
      return;
    }

    setState(() => _loading = true);
    try {
      final token = ref.read(authControllerProvider).accessToken;
      if (token == null) throw Exception('Not authenticated');

      final userService = ref.read(userServiceProvider);
      // Backend expects KeycloakId which we don't have, or does it?
      // POST /api/users documentation: Admin_Org can create users. If keycloakId is not passed, it might fail or auto-generate depending on backend logic. Assuming the backend accepts empty keycloakId if creating a GUEST or handles it.
      final res = await userService.createUser(
        token,
        email: email,
        firstName: firstName,
        lastName: lastName,
        role: _selectedRole,
        phone: phone,
      );

      if (!res.success) {
        throw Exception(res.error?.message ?? 'Failed to create user');
      }

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('User created successfully')));
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
      appBar: AppBar(title: const Text('Create Regular User')),
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
              DropdownButtonFormField<String>(
                value: _selectedRole,
                decoration: const InputDecoration(labelText: 'Role', border: OutlineInputBorder()),
                items: const [
                  DropdownMenuItem(value: 'GUEST', child: Text('Guest')),
                  DropdownMenuItem(value: 'SCANNER', child: Text('Scanner')),
                ],
                onChanged: (val) {
                  if (val != null) setState(() => _selectedRole = val);
                },
              ),
              const SizedBox(height: 24),
              PrimaryButton(label: 'Create User', onPressed: _submit, isLoading: _loading),
            ],
          ),
        ),
      ),
    );
  }
}
