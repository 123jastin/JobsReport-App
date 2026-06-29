import 'package:flutter/material.dart';

class TermsOfServiceScreen extends StatelessWidget {
  const TermsOfServiceScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: const Text('Terms of Service', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.gavel, color: Color(0xFF3B82F6), size: 28),
                const SizedBox(width: 12),
                Text(
                  'Terms of Service',
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                        color: Colors.white,
                        fontWeight: FontWeight.black,
                      ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            const Text(
              'LAST UPDATED: JUNE 2026',
              style: TextStyle(
                color: Color(0xFF64748B),
                fontWeight: FontWeight.bold,
                fontSize: 12,
                fontFamily: 'monospace',
              ),
            ),
            const SizedBox(height: 24),
            const Divider(color: Color(0xFF1E293B)),
            const SizedBox(height: 24),
            _buildSection(
              context,
              '1. Acceptance of Terms',
              'By accessing and using the JobsReport application, you agree to be bound by these Terms of Service. If you do not agree to these terms, please do not use the application.',
            ),
            _buildSection(
              context,
              '2. Appropriate Use',
              'You agree to use this portal strictly for personal, non-commercial recruitment search, networking, and career advice. Scraping job details, automated querying, or reversing telemetry endpoints is strictly prohibited.',
            ),
            _buildSection(
              context,
              '3. Intellectual Property',
              'The structure, designs, reports, and overall assets are proprietary properties of JobsReport.online. You may not reproduce or republish reports without explicit reference or written authorization.',
            ),
            const SizedBox(height: 32),
          ],
        ),
      ),
    );
  }

  Widget _buildSection(BuildContext context, String title, String body) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 24.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                ),
          ),
          const SizedBox(height: 8),
          Text(
            body,
            style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 13, height: 1.6),
          ),
        ],
      ),
    );
  }
}
