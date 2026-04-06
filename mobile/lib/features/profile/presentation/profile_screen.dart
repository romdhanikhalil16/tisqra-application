import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile/core/auth/auth_controller.dart';

class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final auth = ref.watch(authControllerProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Profile')),
      body: auth.userId == null
          ? const Center(child: Text('Not authenticated'))
          : ListView(
              padding: const EdgeInsets.all(16),
              children: [
                Center(
                  child: Column(
                    children: [
                      CircleAvatar(
                        radius: 44,
                        backgroundColor: Theme.of(context).colorScheme.primary.withOpacity(0.12),
                        child: Icon(Icons.person, size: 44, color: Theme.of(context).colorScheme.primary),
                      ),
                      const SizedBox(height: 12),
                      Text(
                        auth.userName ?? 'User',
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        auth.userEmail ?? '',
                        style: Theme.of(context).textTheme.bodyMedium,
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 18),
                Card(
                  elevation: 0,
                  child: Column(
                    children: [
                      ListTile(
                        leading: const Icon(Icons.edit_outlined),
                        title: const Text('Edit profile'),
                        trailing: const Icon(Icons.chevron_right_outlined),
                        onTap: () => context.go('/app/profile/edit'),
                      ),
                      const Divider(height: 1),
                      ListTile(
                        leading: const Icon(Icons.settings_outlined),
                        title: const Text('Settings'),
                        trailing: const Icon(Icons.chevron_right_outlined),
                        onTap: () => context.go('/app/profile/settings'),
                      ),
                      const Divider(height: 1),
                      ListTile(
                        leading: const Icon(Icons.logout_outlined),
                        title: const Text('Logout'),
                        trailing: const Icon(Icons.chevron_right_outlined),
                        onTap: () async {
                          await ref.read(authControllerProvider.notifier).signOut();
                          if (!context.mounted) return;
                          context.go('/login');
                        },
                      ),
                    ],
                  ),
                ),
              ],
            ),
    );
  }
}

