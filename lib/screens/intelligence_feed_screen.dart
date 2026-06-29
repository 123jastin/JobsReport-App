import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/jobs_provider.dart';

class IntelligenceFeedScreen extends StatefulWidget {
  final VoidCallback onNavigateToJobs;

  const IntelligenceFeedScreen({
    super.key,
    required this.onNavigateToJobs,
  });

  @override
  State<IntelligenceFeedScreen> createState() => _IntelligenceFeedScreenState();
}

class _IntelligenceFeedScreenState extends State<IntelligenceFeedScreen> {
  int _liveFeedCount = 128;
  int _activeCampaigns = 42;
  Timer? _simulationTimer;
  final Random _random = Random();

  @override
  void initState() {
    super.initState();
    _simulationTimer = Timer.periodic(const Duration(seconds: 4), (timer) {
      if (mounted) {
        setState(() {
          _liveFeedCount += _random.nextInt(3) + 1;
          if (_random.nextDouble() > 0.7) {
            _activeCampaigns += _random.nextInt(3) - 1;
          }
        });
      }
    });
  }

  @override
  void dispose() {
    _simulationTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<JobsProvider>(context);
    final selectedCountry = provider.selectedCountry;
    final allJobs = provider.allJobs;

    // Filtering indicators based on active country context
    final countryJobs = selectedCountry == 'Worldwide'
        ? allJobs
        : allJobs.where((j) => j.location.toLowerCase().contains(selectedCountry.toLowerCase())).toList();

    // Interactive custom simulated event logs dynamically using real jobs
    final List<String> customLogs = [];
    if (countryJobs.isNotEmpty) {
      countryJobs.take(3).forEach((job) {
        customLogs.add('💼 LIVE ROLE: ${job.company} is hiring a ${job.title} in ${job.location}.');
      });
      customLogs.add('🔋 TELEMETRY: Telecom recruiter activity surged by 12% in $selectedCountry region.');
      customLogs.add('🤖 AI ANALYSIS: Local sector seeks increased experience with Flutter & Material 3 design elements.');
      customLogs.add('📈 TRENDING: Key competencies in $selectedCountry include Dart, Provider, and Asynchronous streams.');
    } else {
      customLogs.add('💼 INTELLIGENCE: Real-time telemetry feed active for $selectedCountry.');
      customLogs.add('🔋 TELEMETRY: System listening for incoming API jobs ingestion campaigns.');
      customLogs.add('🤖 AI ANALYSIS: Applicant matching engines analyzing local demand signals.');
    }

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: const Text('Intelligence Matrix', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(16.0),
          children: [
            // Hero / Header Card
            Card(
              color: const Color(0xFF1E293B),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
                side: BorderSide(color: const Color(0xFF334155).withOpacity(0.5)),
              ),
              child: Padding(
                padding: const EdgeInsets.all(18.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Container(
                          width: 8,
                          height: 8,
                          decoration: const BoxDecoration(color: Color(0xFF10B981), shape: BoxShape.circle),
                        ),
                        const SizedBox(width: 8),
                        const Text(
                          'REAL-TIME INTELLIGENCE MATRIX',
                          style: TextStyle(
                            fontFamily: 'monospace',
                            fontSize: 11,
                            fontWeight: FontWeight.bold,
                            color: Color(0xFF10B981),
                            letterSpacing: 1.0,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Text(
                      selectedCountry == 'Worldwide' ? 'Global Hiring Telemetry' : 'Telemetry: $selectedCountry',
                      style: const TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.black),
                    ),
                    const SizedBox(height: 6),
                    const Text(
                      'Aggregating ingestion metrics, deduplication indices, and live job vacancy telemetry from top employers.',
                      style: TextStyle(color: Color(0xFF94A3B8), fontSize: 13, height: 1.4),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Real-Time System Metrics GRID
            Row(
              children: [
                Expanded(
                  child: _buildMetricCard(
                    'INGESTED VAULT',
                    '$_liveFeedCount',
                    'Real-time records',
                    const Color(0xFF3B82F6),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _buildMetricCard(
                    'ACTIVE RECRUITING',
                    '$_activeCampaigns',
                    'Live campaigns',
                    const Color(0xFF8B5CF6),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),

            // Live Feed Updates (Event logs)
            const Padding(
              padding: EdgeInsets.only(left: 4.0),
              child: Text(
                'LIVE SYSTEM INGEST STREAM',
                style: TextStyle(
                  fontFamily: 'monospace',
                  fontSize: 11,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF64748B),
                  letterSpacing: 1.0,
                ),
              ),
            ),
            const SizedBox(height: 12),

            ...customLogs.map((log) {
              return Padding(
                padding: const EdgeInsets.only(bottom: 12.0),
                child: Card(
                  color: const Color(0xFF1E293B).withOpacity(0.8),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                    side: BorderSide(color: const Color(0xFF334155).withOpacity(0.3)),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.all(14.0),
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Padding(
                          padding: const EdgeInsets.only(top: 5.0),
                          child: Container(
                            width: 6,
                            height: 6,
                            decoration: const BoxDecoration(color: Color(0xFF3B82F6), shape: BoxShape.circle),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Text(
                            log,
                            style: const TextStyle(
                              color: Color(0xFFE2E8F0),
                              fontFamily: 'monospace',
                              fontSize: 12,
                              height: 1.4,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              );
            }).toList(),

            const SizedBox(height: 16),

            // Call To Action to View All Jobs
            SizedBox(
              height: 52,
              child: ElevatedButton.icon(
                onPressed: widget.onNavigateToJobs,
                icon: const Icon(Icons.trending_up, color: Colors.white),
                label: Text(
                  'EXPLORE ACTIVE FEED (${countryJobs.length} JOBS AVAILABLE)',
                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, letterSpacing: 0.5),
                ),
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF3B82F6),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMetricCard(String title, String value, String sub, Color tintColor) {
    return Card(
      color: const Color(0xFF1E293B),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: const Color(0xFF334155).withOpacity(0.5)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: const TextStyle(
                fontFamily: 'monospace',
                fontSize: 10,
                fontWeight: FontWeight.bold,
                color: Color(0xFF94A3B8),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              value,
              style: TextStyle(color: tintColor, fontWeight: FontWeight.black, fontSize: 24),
            ),
            const SizedBox(height: 2),
            Text(
              sub,
              style: const TextStyle(color: Color(0xFF64748B), fontSize: 11),
            ),
          ],
        ),
      ),
    );
  }
}
