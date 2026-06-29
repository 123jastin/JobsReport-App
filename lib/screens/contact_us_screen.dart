import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

class ContactUsScreen extends StatelessWidget {
  const ContactUsScreen({super.key});

  Future<void> _sendEmail() async {
    final Uri emailLaunchUri = Uri(
      scheme: 'mailto',
      path: 'jjovinatha@gmail.com',
      queryParameters: {
        'subject': 'JobsReport Query / Feedback',
      },
    );
    if (await canLaunchUrl(emailLaunchUri)) {
      await launchUrl(emailLaunchUri);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: const Text('Contact Us', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Get In Touch',
              style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                    color: Colors.white,
                    fontWeight: FontWeight.black,
                  ),
            ),
            const SizedBox(height: 8),
            const Text(
              'Have any queries, feedback, or job listings to submit? We are always happy to hear from you.',
              style: TextStyle(color: Color(0xFF94A3B8), fontSize: 14, height: 1.5),
            ),
            const SizedBox(height: 24),
            const Divider(color: Color(0xFF1E293B)),
            const SizedBox(height: 24),

            // Email Card
            Card(
              color: const Color(0xFF1E293B).withOpacity(0.4),
              child: ListTile(
                leading: Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: const Color(0xFF3B82F6).withOpacity(0.12),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(Icons.email, color: Color(0xFF3B82F6)),
                ),
                title: const Text('Email Address', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white)),
                subtitle: const Text('jjovinatha@gmail.com', style: TextStyle(color: Color(0xFF94A3B8))),
                trailing: const Icon(Icons.chevron_right, color: Color(0xFF64748B)),
                onTap: _sendEmail,
              ),
            ),
            const SizedBox(height: 12),

            // Location card
            Card(
              color: const Color(0xFF1E293B).withOpacity(0.4),
              child: ListTile(
                leading: Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: const Color(0xFF10B981).withOpacity(0.12),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(Icons.location_on, color: Color(0xFF10B981)),
                ),
                title: const Text('Our Head Office', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white)),
                subtitle: const Text('Dar es Salaam, Tanzania', style: TextStyle(color: Color(0xFF94A3B8))),
              ),
            ),
            const SizedBox(height: 12),

            // Telegram/WhatsApp placeholder card
            Card(
              color: const Color(0xFF1E293B).withOpacity(0.4),
              child: ListTile(
                leading: Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: const Color(0xFF8B5CF6).withOpacity(0.12),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(Icons.forum, color: Color(0xFF8B5CF6)),
                ),
                title: const Text('Community Channels', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white)),
                subtitle: const Text('Join the conversation on WhatsApp & FB', style: TextStyle(color: Color(0xFF94A3B8))),
              ),
            ),
            const SizedBox(height: 48),

            Center(
              child: ElevatedButton.icon(
                onPressed: _sendEmail,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF2563EB),
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                ),
                icon: const Icon(Icons.mail),
                label: const Text('Send an Email', style: TextStyle(fontWeight: FontWeight.bold)),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
