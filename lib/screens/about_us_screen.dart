import 'package:flutter/material.dart';

class AboutUsScreen extends StatelessWidget {
  const AboutUsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: const Text('About Us', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.auto_awesome, color: Color(0xFF3B82F6), size: 28),
                const SizedBox(width: 12),
                Text(
                  'JobsReport.online',
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                        color: Colors.white,
                        fontWeight: FontWeight.black,
                      ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            const Text(
              'Real-Time Job Market Intelligence Platform',
              style: TextStyle(
                color: Color(0xFF10B981),
                fontWeight: FontWeight.bold,
                fontSize: 14,
                fontFamily: 'monospace',
              ),
            ),
            const SizedBox(height: 24),
            const Divider(color: Color(0xFF1E293B)),
            const SizedBox(height: 24),
            Text(
              'Our Vision',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
            ),
            const SizedBox(height: 8),
            const Text(
              'To bring complete transparency, data-driven telemetry, and actionable insight to job seekers and employers across East Africa and globally. We believe finding a job shouldn\'t be based on mystery, but on clear signals of demand, skill requirements, and market salary estimates.',
              style: TextStyle(color: Color(0xFF94A3B8), fontSize: 14, height: 1.6),
            ),
            const SizedBox(height: 24),
            Text(
              'Who We Are',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
            ),
            const SizedBox(height: 8),
            const Text(
              'JobsReport is an independent talent intelligence aggregator. We monitor hiring trends, aggregate vacant positions directly from verified employers, and generate detailed market report analyses. Our specialized AI-powered career assistants help candidates align their profiles with what hiring managers are actively seeking.',
              style: TextStyle(color: Color(0xFF94A3B8), fontSize: 14, height: 1.6),
            ),
            const SizedBox(height: 48),
            Center(
              child: Text(
                '© 2026 JobsReport • Made with Pride in Tanzania',
                style: Theme.of(context).textTheme.labelSmall?.copyWith(
                      color: const Color(0xFF475569),
                      fontFamily: 'monospace',
                    ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
