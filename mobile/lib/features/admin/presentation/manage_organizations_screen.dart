import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/features/admin/data/organization_service.dart';

class ManageOrganizationsScreen extends ConsumerStatefulWidget {
  const ManageOrganizationsScreen({super.key});

  @override
  ConsumerState<ManageOrganizationsScreen> createState() => _ManageOrganizationsScreenState();
}

class _ManageOrganizationsScreenState extends ConsumerState<ManageOrganizationsScreen> {
  bool _isLoading = true;
  List<dynamic> _organizations = [];
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadOrganizations();
  }

  Future<void> _loadOrganizations() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final token = ref.read(authControllerProvider).accessToken;
      if (token == null) throw Exception('Not authenticated');

      final orgService = ref.read(organizationServiceProvider);
      final response = await orgService.getOrganizations(token);
      
      if (!response.success) {
        throw Exception(response.error?.message ?? 'Failed to load organizations');
      }

      final data = response.data as Map<String, dynamic>;
      final content = data['content'] as List<dynamic>? ?? [];
      
      setState(() {
        _organizations = content;
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
        title: const Text('Organizations'),
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
                        onPressed: _loadOrganizations,
                        child: const Text('Retry'),
                      ),
                    ],
                  ),
                )
              : RefreshIndicator(
                  onRefresh: _loadOrganizations,
                  child: _organizations.isEmpty
                      ? const Center(child: Text('No organizations found.'))
                      : ListView.builder(
                          itemCount: _organizations.length,
                          itemBuilder: (context, index) {
                            final org = _organizations[index];
                            return ListTile(
                              leading: const CircleAvatar(child: Icon(Icons.business)),
                              title: Text(org['name'] ?? 'Unnamed Org'),
                              subtitle: Text(org['email'] ?? ''),
                              trailing: Chip(label: Text(org['status'] ?? 'ACTIVE')),
                            );
                          },
                        ),
                ),
    );
  }
}
