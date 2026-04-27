import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/core/network/api_response.dart';
import 'package:mobile/features/admin/data/user_service.dart';

class ListOrgUsersScreen extends ConsumerStatefulWidget {
  const ListOrgUsersScreen({super.key});

  @override
  ConsumerState<ListOrgUsersScreen> createState() => _ListOrgUsersScreenState();
}

class _ListOrgUsersScreenState extends ConsumerState<ListOrgUsersScreen> {
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
      
      setState(() {
        _users = content;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  Future<void> _performAction(String userId, String action) async {
    try {
      final token = ref.read(authControllerProvider).accessToken;
      if (token == null) throw Exception('Not authenticated');

      final userService = ref.read(userServiceProvider);
      ApiResponse response;

      switch (action) {
        case 'activate':
          response = await userService.activateUser(token, userId);
          break;
        case 'deactivate':
          response = await userService.deactivateUser(token, userId);
          break;
        case 'delete':
          response = await userService.deleteUser(token, userId);
          break;
        default:
          throw Exception('Unknown action');
      }

      if (!response.success) {
        throw Exception(response.error?.message ?? 'Failed to $action user');
      }

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('User $action successful')),
        );
        _loadUsers();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Organization Users'),
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
                      ? const Center(child: Text('No users found.'))
                      : ListView.builder(
                          itemCount: _users.length,
                          itemBuilder: (context, index) {
                            final user = _users[index];
                            return ListTile(
                              leading: const CircleAvatar(child: Icon(Icons.person)),
                              title: Text('${user['firstName']} ${user['lastName']}'),
                              subtitle: Text(user['email'] ?? ''),
                              trailing: Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  Chip(label: Text(user['role'] ?? '')),
                                  PopupMenuButton<String>(
                                    onSelected: (action) => _performAction(user['id'], action),
                                    itemBuilder: (context) => [
                                      const PopupMenuItem(value: 'activate', child: Text('Activate')),
                                      const PopupMenuItem(value: 'deactivate', child: Text('Deactivate')),
                                      const PopupMenuItem(value: 'delete', child: Text('Delete', style: TextStyle(color: Colors.red))),
                                    ],
                                  ),
                                ],
                              ),
                            );
                          },
                        ),
                ),
    );
  }
}
