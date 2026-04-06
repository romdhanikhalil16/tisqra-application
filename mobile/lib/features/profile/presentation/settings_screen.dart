import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/core/theme/theme_controller.dart';
import 'package:go_router/go_router.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SettingsScreen extends ConsumerStatefulWidget {
  const SettingsScreen({super.key});

  @override
  ConsumerState<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends ConsumerState<SettingsScreen> {
  bool _notificationsEnabled = true;
  bool _loadingPrefs = true;
  String _language = 'English';

  @override
  void initState() {
    super.initState();
    _loadPrefs();
  }

  Future<void> _loadPrefs() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _notificationsEnabled = prefs.getBool('notifications_enabled') ?? true;
      _language = prefs.getString('language') ?? 'English';
      _loadingPrefs = false;
    });
  }

  Future<void> _setNotificationsEnabled(bool value) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('notifications_enabled', value);
    setState(() => _notificationsEnabled = value);
  }

  Future<void> _setLanguage(String value) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('language', value);
    setState(() => _language = value);
  }

  @override
  Widget build(BuildContext context) {
    final themeMode = ref.watch(themeModeProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings'),
      ),
      body: _loadingPrefs
          ? const Center(child: CircularProgressIndicator())
          : ListView(
              padding: const EdgeInsets.all(16),
              children: [
                const Text('Appearance', style: TextStyle(fontWeight: FontWeight.w700)),
                const SizedBox(height: 10),
                Card(
                  elevation: 0,
                  child: Padding(
                    padding: const EdgeInsets.all(12),
                    child: Column(
                      children: [
                        ListTile(
                          contentPadding: EdgeInsets.zero,
                          title: const Text('Dark mode'),
                          trailing: Switch(
                            value: themeMode == ThemeMode.dark,
                            onChanged: (v) {
                              ref
                                  .read(themeModeProvider.notifier)
                                  .setThemeMode(v ? ThemeMode.dark : ThemeMode.light);
                            },
                          ),
                        ),
                        const Divider(height: 1),
                        ListTile(
                          contentPadding: EdgeInsets.zero,
                          title: const Text('Notifications'),
                          subtitle: const Text('In-app notification toggle'),
                          trailing: Switch(
                            value: _notificationsEnabled,
                            onChanged: _setNotificationsEnabled,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 18),
                const Text('Language', style: TextStyle(fontWeight: FontWeight.w700)),
                const SizedBox(height: 10),
                Card(
                  elevation: 0,
                  child: Padding(
                    padding: const EdgeInsets.all(12),
                    child: DropdownButtonFormField<String>(
                      initialValue: _language,
                      items: const [
                        DropdownMenuItem(value: 'English', child: Text('English')),
                        DropdownMenuItem(value: 'French', child: Text('French')),
                        DropdownMenuItem(value: 'Arabic', child: Text('Arabic')),
                      ],
                      onChanged: (v) {
                        if (v == null) return;
                        _setLanguage(v);
                      },
                      decoration: const InputDecoration(
                        border: OutlineInputBorder(),
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 18),
                Card(
                  color: Theme.of(context).colorScheme.errorContainer.withOpacity(0.4),
                  elevation: 0,
                  child: ListTile(
                    leading: Icon(Icons.logout_outlined,
                        color: Theme.of(context).colorScheme.error),
                    title: Text('Logout', style: TextStyle(color: Theme.of(context).colorScheme.error)),
                    trailing: const Icon(Icons.chevron_right_outlined),
                    onTap: () async {
                      await ref.read(authControllerProvider.notifier).signOut();
                      if (!context.mounted) return;
                      context.go('/login');
                    },
                  ),
                ),
              ],
            ),
    );
  }
}

