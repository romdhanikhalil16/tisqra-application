import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/core/providers/api_providers.dart';
import 'package:mobile/shared/widgets/app_text_field.dart';
import 'package:mobile/shared/widgets/primary_button.dart';

class EditProfileScreen extends ConsumerStatefulWidget {
  const EditProfileScreen({super.key});

  @override
  ConsumerState<EditProfileScreen> createState() => _EditProfileScreenState();
}

class _EditProfileScreenState extends ConsumerState<EditProfileScreen> {
  final _nameCtrl = TextEditingController();
  final _emailCtrl = TextEditingController();
  final _phoneCtrl = TextEditingController();
  bool _loading = false;

  @override
  void initState() {
    super.initState();
    final auth = ref.read(authControllerProvider);
    _nameCtrl.text = auth.userName ?? '';
    _emailCtrl.text = auth.userEmail ?? '';
  }

  @override
  void dispose() {
    _nameCtrl.dispose();
    _emailCtrl.dispose();
    _phoneCtrl.dispose();
    super.dispose();
  }

  Future<void> _onSave() async {
    final auth = ref.read(authControllerProvider);
    final userId = auth.userId;
    final token = auth.accessToken;
    if (userId == null || token == null) return;

    final name = _nameCtrl.text.trim();
    final parts = name.split(RegExp(r'\s+')).where((p) => p.isNotEmpty).toList();
    final firstName = parts.isNotEmpty ? parts.first : '';
    final lastName = parts.length > 1 ? parts.sublist(1).join(' ') : '';
    final phone = _phoneCtrl.text.trim().isEmpty ? null : _phoneCtrl.text.trim();

    if (firstName.isEmpty || lastName.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Enter both first name and last name')),
      );
      return;
    }

    setState(() => _loading = true);
    try {
      final apiClient = ref.read(apiClientProvider);
      final resp = await apiClient.putApiResponseDynamic(
        '/api/users/$userId',
        bearerToken: token,
        body: {
          'firstName': firstName,
          'lastName': lastName,
          'phone': phone,
          'profileImageUrl': null,
        },
      );
      if (!resp.success) {
        throw Exception(resp.error?.message ?? 'Update failed');
      }
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Profile updated')),
      );
      // refresh session data by re-login if needed
      context.pop();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(e.toString()), backgroundColor: Colors.red),
      );
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Edit profile')),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: ListView(
            children: [
              const Text(
                'Update your details',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800),
              ),
              const SizedBox(height: 18),
              AppTextField(
                controller: _nameCtrl,
                label: 'Name',
                hint: 'John Doe',
                textInputAction: TextInputAction.next,
              ),
              const SizedBox(height: 12),
              TextField(
                controller: _emailCtrl,
                enabled: false,
                decoration: const InputDecoration(
                  labelText: 'Email',
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 12),
              AppTextField(
                controller: _phoneCtrl,
                label: 'Phone (optional)',
                hint: '+216...',
                keyboardType: TextInputType.phone,
                textInputAction: TextInputAction.done,
              ),
              const SizedBox(height: 18),
              PrimaryButton(
                label: 'Save changes',
                isLoading: _loading,
                onPressed: _onSave,
                icon: Icons.save_outlined,
              ),
              const SizedBox(height: 14),
              OutlinedButton.icon(
                onPressed: () => context.go('/forgot-password'),
                icon: const Icon(Icons.lock_reset_outlined),
                label: const Text('Change password'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

