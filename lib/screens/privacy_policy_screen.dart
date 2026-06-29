import 'package:flutter/material.dart';

class PrivacyPolicyScreen extends StatelessWidget {
  const PrivacyPolicyScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: const Text('Privacy Policy', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.security, color: Color(0xFF10B981), size: 28),
                const SizedBox(width: 12),
                Text(
                  'Privacy Policy',
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
              '1. Data Collection',
              'We collect minimal personal data. When you fill out your User Profile inside the application, the details (full name, email, target job title, experience, bio) are stored strictly locally on your device in SharedPreferences. We do not transmit your profile details to any background server unless you explicitly submit a resume review or bio request to our Gemini AI assistants.',
            ),
            _buildSection(
              context,
              '2. Telemetry and Analytics',
              'We may collect anonymous usage statistics like job views, clicks, and general application routing events to improve our market vacancy mapping algorithms.',
            ),
            _buildSection(
              context,
              '3. Third-Party Integrations',
              'When you click on job application links or view external recruitment portals, those third-party websites operate under their own independent privacy agreements. We advise you to read them when navigating away from JobsReport.',
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
