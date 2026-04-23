import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/features/admin/data/user_service.dart';

class ListOrgAdminsScreen extends ConsumerStatefulWidget {
  const ListOrgAdminsScreen({super.key});

  @override
  ConsumerState<ListOrgAdminsScreen> createState() => _ListOrgAdminsScreenState();
}

class _ListOrgAdminsScreenState extends ConsumerState<ListOrgAdminsScreen> {
  bool _isLoading = true;
  List<dynamic> _users = [];
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadUsers();
  }

  Future<void> _loadUsers() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final token = ref.read(authControllerProvider).accessToken;
      if (token == null) throw Exception('Not authenticated');

      final userService = ref.read(userServiceProvider);
      final response = await userService.getUsers(token);
      
      if (!response.success) {
        throw Exception(response.error?.message ?? 'Failed to load users');
      }

      final data = response.data as Map<String, dynamic>;
      final content = data['content'] as List<dynamic>? ?? [];
      
      // Filter just to see ADMIN_ORG for demo purposes, or show all
      setState(() {
        _users = content.where((u) => u['role'] == 'ADMIN_ORG').toList();
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Organization Admins'),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(_error!, style: const TextStyle(color: Colors.red)),
                      const SizedBox(height: 16),
                      ElevatedButton(
                        onPressed: _loadUsers,
                        child: const Text('Retry'),
                      ),
                    ],
                  ),
                )
              : RefreshIndicator(
                  onRefresh: _loadUsers,
                  child: _users.isEmpty
                      ? const Center(child: Text('No organization admins found.'))
                      : ListView.builder(
                          itemCount: _users.length,
                          itemBuilder: (context, index) {
                            final user = _users[index];
                            return ListTile(
                              leading: const CircleAvatar(child: Icon(Icons.business)),
                              title: Text('${user['firstName']} ${user['lastName']}'),
                              subtitle: Text(user['email'] ?? ''),
                              trailing: Chip(label: Text(user['role'] ?? '')),
                            );
                          },
                        ),
                ),
    );
  }
}
