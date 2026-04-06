class PageResult<T> {
  final List<T> content;
  final int page;
  final int size;
  final int totalElements;
  final int totalPages;

  const PageResult({
    required this.content,
    required this.page,
    required this.size,
    required this.totalElements,
    required this.totalPages,
  });

  factory PageResult.fromJson(
    dynamic json, {
    required T Function(dynamic item) itemParser,
  }) {
    final map = (json as Map<String, dynamic>);

    final contentJson = map['content'] as List<dynamic>? ?? const [];
    final content = contentJson.map(itemParser).toList();

    return PageResult<T>(
      content: content,
      page: (map['number'] as num?)?.toInt() ?? 0,
      size: (map['size'] as num?)?.toInt() ?? 0,
      totalElements: (map['totalElements'] as num?)?.toInt() ?? 0,
      totalPages: (map['totalPages'] as num?)?.toInt() ?? 0,
    );
  }
}

