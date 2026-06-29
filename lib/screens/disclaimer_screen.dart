import 'package:flutter/material.dart';

class DisclaimerScreen extends StatelessWidget {
  const DisclaimerScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: const Text('Disclaimer', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.warning_amber_rounded, color: Color(0xFFF59E0B), size: 28),
                const SizedBox(width: 12),
                Text(
                  'Legal Disclaimer',
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                        color: Colors.white,
                        fontWeight: FontWeight.black,
                      ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            const Text(
              'PLEASE READ CAREFULLY BEFORE USING OUR PORTAL',
              style: TextStyle(
                color: Color(0xFFEF4444),
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
              'Job Listing Authenticity',
              'JobsReport aggregates hiring signals and listings from various public sources, employer submissions, and API feeds. While we endeavor to verify the background of every post, we do not guarantee the accuracy, completeness, or absolute legitimacy of any listing, description, or attachment.',
            ),
            _buildSection(
              context,
              'Stay Safe Online (Zero-Fee Policy)',
              'JobsReport strictly enforces a ZERO recruitment fee policy. Under no circumstances should an applicant ever pay any money, buying assets, paying for training, processing fees, or medical exams to secure an interview. We are not liable for any financial losses or damages resulting from third-party fraudulent behavior.',
            ),
            _buildSection(
              context,
              'Limitation of Liability',
              'The platform, statistics, reports, and AI advices are provided on an "as-is" basis. JobsReport, its parent entity, and its developers explicitly disclaim any liability for decisions made based on telemetry statistics or AI responses provided inside this application.',
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
