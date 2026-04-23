import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/shared/widgets/app_text_field.dart';
import 'package:mobile/shared/widgets/primary_button.dart';

class RegisterScreen extends ConsumerStatefulWidget {
  const RegisterScreen({super.key});

  @override
  ConsumerState<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends ConsumerState<RegisterScreen> {
  final _firstName = TextEditingController();
  final _lastName = TextEditingController();
  final _email = TextEditingController();
  final _phone = TextEditingController();
  final _password = TextEditingController();
  final _confirm = TextEditingController();
  bool _terms = false;
  bool _obscure = true;
  bool _obscure2 = true;
  bool _loading = false;

  @override
  void dispose() {
    _firstName.dispose();
    _lastName.dispose();
    _email.dispose();
    _phone.dispose();
    _password.dispose();
    _confirm.dispose();
    super.dispose();
  }

  double _strengthScore(String password) {
    if (password.isEmpty) return 0;
    var score = 0.0;
    if (password.length >= 8) score += 0.25;
    if (password.length >= 12) score += 0.25;
    if (RegExp(r'[A-Z]').hasMatch(password)) score += 0.15;
    if (RegExp(r'[0-9]').hasMatch(password)) score += 0.15;
    if (RegExp(r'[^A-Za-z0-9]').hasMatch(password)) score += 0.2;
    return score.clamp(0, 1);
  }

  String _strengthLabel(double score) {
    if (score < 0.35) return 'Weak';
    if (score < 0.7) return 'Good';
    return 'Strong';
  }

  String _toReadableError(Object error) {
    final msg = error.toString();
    if (msg.startsWith('Exception: ')) {
      return msg.substring('Exception: '.length);
    }
    return msg;
  }

  Future<void> _onRegister() async {
    final firstName = _firstName.text.trim();
    final lastName = _lastName.text.trim();
    final email = _email.text.trim();
    final phone = _phone.text.trim().isEmpty ? null : _phone.text.trim();
    final password = _password.text;
    final confirm = _confirm.text;

    String? error;
    if (firstName.isEmpty) error = 'First name is required';
    if (error == null && lastName.isEmpty) error = 'Last name is required';
    
    final emailRegex = RegExp(r'^[^@\s]+@[^@\s]+\.[^@\s]+$');
    if (error == null && !emailRegex.hasMatch(email)) error = 'Enter a valid email';
    
    if (error == null && phone != null && !RegExp(r'^\+?[1-9]\d{1,14}$').hasMatch(phone)) {
      error = 'Enter a valid phone number';
    }

    if (error == null && password.length < 8) error = 'Password must be 8+ chars';
    if (error == null && password != confirm) error = 'Passwords do not match';
    if (error == null && !_terms) error = 'Please accept the terms & conditions';

    if (error != null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(error), backgroundColor: Colors.red),
      );
      return;
    }

    setState(() => _loading = true);
    try {
      await ref.read(authControllerProvider.notifier).signUp(
            email: email,
            password: password,
            firstName: firstName,
            lastName: lastName,
            phone: phone,
          );
      if (!mounted) return;
      context.go('/verify?email=$email');
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(_toReadableError(e)), backgroundColor: Colors.red),
      );
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final score = _strengthScore(_password.text);

    return Scaffold(
      appBar: AppBar(title: const Text('Create account')),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(20),
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 520),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text('Join Tisqra', style: Theme.of(context).textTheme.headlineSmall),
                const SizedBox(height: 8),
                Text(
                  'Create an account to start buying tickets and managing orders.',
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
                const SizedBox(height: 18),
                Row(
                  children: [
                    Expanded(
                      child: AppTextField(
                        controller: _firstName,
                        label: 'First name',
                        hint: 'John',
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: AppTextField(
                        controller: _lastName,
                        label: 'Last name',
                        hint: 'Doe',
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                AppTextField(
                  controller: _email,
                  label: 'Email',
                  hint: 'john@example.com',
                  keyboardType: TextInputType.emailAddress,
                ),
                const SizedBox(height: 12),
                AppTextField(
                  controller: _phone,
                  label: 'Phone (optional)',
                  hint: '+216...',
                  keyboardType: TextInputType.phone,
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: _password,
                  obscureText: _obscure,
                  decoration: InputDecoration(
                    labelText: 'Password',
                    border: const OutlineInputBorder(),
                    suffixIcon: IconButton(
                      icon: Icon(_obscure
                          ? Icons.visibility_off_outlined
                          : Icons.visibility_outlined),
                      onPressed: () => setState(() => _obscure = !_obscure),
                    ),
                  ),
                ),
                const SizedBox(height: 10),
                LinearProgressIndicator(value: score, minHeight: 8),
                const SizedBox(height: 6),
                Text(
                  'Strength: ${_strengthLabel(score)}',
                  style: Theme.of(context).textTheme.bodySmall,
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: _confirm,
                  obscureText: _obscure2,
                  decoration: InputDecoration(
                    labelText: 'Confirm password',
                    border: const OutlineInputBorder(),
                    suffixIcon: IconButton(
                      icon: Icon(_obscure2
                          ? Icons.visibility_off_outlined
                          : Icons.visibility_outlined),
                      onPressed: () => setState(() => _obscure2 = !_obscure2),
                    ),
                  ),
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Checkbox(
                      value: _terms,
                      onChanged: (v) => setState(() => _terms = v ?? false),
                    ),
                    Expanded(
                      child: Text(
                        'I agree to the Terms & Conditions and Privacy Policy.',
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 18),
                PrimaryButton(
                  label: 'Register',
                  isLoading: _loading,
                  onPressed: _onRegister,
                  icon: Icons.person_add_outlined,
                ),
                const SizedBox(height: 14),
                OutlinedButton(
                  onPressed: () => context.go('/login'),
                  child: const Text('Already have an account? Login'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

