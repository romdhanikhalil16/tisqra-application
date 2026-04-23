import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/core/auth/auth_controller.dart';
import 'package:mobile/features/admin/data/event_service.dart';

class ManageEventsScreen extends ConsumerStatefulWidget {
  const ManageEventsScreen({super.key});

  @override
  ConsumerState<ManageEventsScreen> createState() => _ManageEventsScreenState();
}

class _ManageEventsScreenState extends ConsumerState<ManageEventsScreen> {
  bool _isLoading = true;
  List<dynamic> _events = [];
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadEvents();
  }

  Future<void> _loadEvents() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final token = ref.read(authControllerProvider).accessToken;
      if (token == null) throw Exception('Not authenticated');

      final eventService = ref.read(eventServiceProvider);
      final response = await eventService.getEvents(token);
      
      if (!response.success) {
        throw Exception(response.error?.message ?? 'Failed to load events');
      }

      final data = response.data as Map<String, dynamic>;
      final content = data['content'] as List<dynamic>? ?? [];
      
      setState(() {
        _events = content;
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
        title: const Text('Manage Events'),
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
                        onPressed: _loadEvents,
                        child: const Text('Retry'),
                      ),
                    ],
                  ),
                )
              : RefreshIndicator(
                  onRefresh: _loadEvents,
                  child: _events.isEmpty
                      ? const Center(child: Text('No events found.'))
                      : ListView.builder(
                          itemCount: _events.length,
                          itemBuilder: (context, index) {
                            final event = _events[index];
                            return ListTile(
                              leading: const CircleAvatar(child: Icon(Icons.event)),
                              title: Text(event['name'] ?? 'Unnamed Event'),
                              subtitle: Text(event['category'] ?? ''),
                              trailing: Chip(label: Text(event['status'] ?? 'DRAFT')),
                            );
                          },
                        ),
                ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          // TODO: Implement Create Event flow
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Create event flow to be implemented')),
          );
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}
