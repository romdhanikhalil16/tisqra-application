import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

class SavedCard {
  final String id;
  final String cardNumber;
  final String cardHolderName;
  final String expiryMonth;
  final String expiryYear;

  const SavedCard({
    required this.id,
    required this.cardNumber,
    required this.cardHolderName,
    required this.expiryMonth,
    required this.expiryYear,
  });

  String maskNumber() {
    final digits = cardNumber.replaceAll(RegExp(r'\D'), '');
    if (digits.length < 4) return '****';
    final last4 = digits.substring(digits.length - 4);
    return '**** $last4';
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'cardNumber': cardNumber,
        'cardHolderName': cardHolderName,
        'expiryMonth': expiryMonth,
        'expiryYear': expiryYear,
      };

  factory SavedCard.fromJson(Map<String, dynamic> json) => SavedCard(
        id: json['id']?.toString() ?? '',
        cardNumber: json['cardNumber']?.toString() ?? '',
        cardHolderName: json['cardHolderName']?.toString() ?? '',
        expiryMonth: json['expiryMonth']?.toString() ?? '',
        expiryYear: json['expiryYear']?.toString() ?? '',
      );
}

class SavedCardsStore {
  static const _kCards = 'saved_cards';

  Future<List<SavedCard>> loadCards() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString(_kCards);
    if (raw == null || raw.isEmpty) return const [];
    final list = json.decode(raw) as List<dynamic>;
    return list.map((e) => SavedCard.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<void> saveCards(List<SavedCard> cards) async {
    final prefs = await SharedPreferences.getInstance();
    final raw = json.encode(cards.map((e) => e.toJson()).toList());
    await prefs.setString(_kCards, raw);
  }

  Future<void> addCard(SavedCard card) async {
    final cards = await loadCards();
    final updated = [...cards, card];
    await saveCards(updated);
  }

  Future<void> deleteCard(String id) async {
    final cards = await loadCards();
    await saveCards(cards.where((c) => c.id != id).toList());
  }
}

