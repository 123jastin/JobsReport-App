import 'dart:math';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:share_plus/share_plus.dart';
import '../providers/jobs_provider.dart';
import '../models/models.dart';

class ReportDetailScreen extends StatefulWidget {
  final String slug;
  final Function(int) onNavigateToJobDetail;

  const ReportDetailScreen({
    super.key,
    required this.slug,
    required this.onNavigateToJobDetail,
  });

  @override
  State<ReportDetailScreen> createState() => _ReportDetailScreenState();
}

class _ReportDetailScreenState extends State<ReportDetailScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<JobsProvider>(context, listen: false).fetchReportDetail(widget.slug);
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<JobsProvider>(context);
    final report = provider.liveReportDetail;
    final isLoading = provider.isReportDetailLoading;

    const bgDark = Color(0xFF0F172A);
    const cardBg = Color(0xFF1E293B);
    const accentBlue = Color(0xFF3B82F6);
    const textMuted = Color(0xFF94A3B8);
    final borderCol = const Color(0xFF334155).withOpacity(0.5);

    if (isLoading) {
      return const Scaffold(
        backgroundColor: bgDark,
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              CircularProgressIndicator(color: accentBlue),
              SizedBox(height: 16),
              Text(
                'SYNCING MARKET INTELLIGENCE DATA...',
                style: TextStyle(fontFamily: 'monospace', fontWeight: FontWeight.bold, color: textMuted, letterSpacing: 1.0),
              ),
            ],
          ),
        ),
      );
    }

    if (report == null) {
      return Scaffold(
        backgroundColor: bgDark,
        appBar: AppBar(
          title: const Text('Briefing Not Found', style: TextStyle(fontWeight: FontWeight.bold)),
        ),
        body: Center(
          child: Padding(
            padding: const EdgeInsets.all(24.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.info_outlined, color: textMuted, size: 64),
                const SizedBox(height: 16),
                const Text(
                  'Briefing Not Found',
                  style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 20),
                ),
                const SizedBox(height: 8),
                const Text(
                  'The requested market intelligence report could not be found or has been archived.',
                  style: TextStyle(color: textMuted, fontSize: 14),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 24),
                ElevatedButton(
                  onPressed: () => Navigator.pop(context),
                  style: ElevatedButton.styleFrom(backgroundColor: accentBlue),
                  child: const Text('Back to Intel Feed', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                ),
              ],
            ),
          ),
        ),
      );
    }

    final stats = report.stats;
    final chartData = report.chartData ?? [];
    final distribution = report.distribution ?? [];
    final jobs = report.jobs ?? [];

    // Filter active/expired placements
    final today = DateTime.now();
    final sortedJobs = List<RemoteJob>.from(jobs);
    sortedJobs.sort((a, b) {
      final aActive = a.active ?? true;
      final bActive = b.active ?? true;
      if (aActive && !bActive) return -1;
      if (!aActive && bActive) return 1;
      final aDate = a.postedAt ?? a.datePosted ?? '';
      final bDate = b.postedAt ?? b.datePosted ?? '';
      return bDate.compareTo(aDate);
    });

    return Scaffold(
      backgroundColor: bgDark,
      appBar: AppBar(
        title: const Text('Intelligence Briefing', style: TextStyle(fontWeight: FontWeight.bold)),
        actions: [
          IconButton(
            icon: const Icon(Icons.share, color: Colors.white),
            onPressed: () {
              Share.share('Read this awesome job market intelligence briefing: ${report.title} - https://jobsreport.online/report/${widget.slug}');
            },
          ),
        ],
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(16.0),
          children: [
            // Breadcrumbs Navigation
            Row(
              children: [
                InkWell(
                  onTap: () => Navigator.pop(context),
                  child: const Text('Home', style: TextStyle(color: textMuted, fontSize: 11)),
                ),
                const Icon(Icons.chevron_right, color: textMuted, size: 12),
                InkWell(
                  onTap: () => Navigator.pop(context),
                  child: const Text('Reports', style: TextStyle(color: textMuted, fontSize: 11)),
                ),
                if (report.country != null && report.country!.isNotEmpty) ...[
                  const Icon(Icons.chevron_right, color: textMuted, size: 12),
                  Text(report.country!, style: const TextStyle(color: textMuted, fontSize: 11)),
                ],
              ],
            ),
            const SizedBox(height: 16),

            // Header Title Block
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Container(
                      width: 6,
                      height: 6,
                      decoration: const BoxDecoration(color: Color(0xFF10B981), shape: BoxShape.circle),
                    ),
                    const SizedBox(width: 8),
                    const Text(
                      'MARKET ANALYSIS / INTELLIGENCE',
                      style: TextStyle(
                        fontFamily: 'monospace',
                        fontWeight: FontWeight.bold,
                        fontSize: 10,
                        letterSpacing: 1.0,
                        color: Color(0xFF10B981),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Text(
                  report.title ?? 'Analysis Briefing',
                  style: const TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.black, height: 1.2),
                ),
                const SizedBox(height: 12),

                // Metadata Details
                Row(
                  children: [
                    const Icon(Icons.date_range, color: textMuted, size: 12),
                    const SizedBox(width: 4),
                    Text(report.monthYear ?? 'Recently', style: const TextStyle(color: textMuted, fontSize: 11)),
                    if (report.role != null && report.role!.isNotEmpty) ...[
                      const SizedBox(width: 12),
                      const Icon(Icons.work, color: textMuted, size: 12),
                      const SizedBox(width: 4),
                      Text(report.role!.toUpperCase(), style: const TextStyle(color: textMuted, fontSize: 11)),
                    ],
                    if (report.country != null && report.country!.isNotEmpty) ...[
                      const SizedBox(width: 12),
                      const Icon(Icons.place, color: textMuted, size: 12),
                      const SizedBox(width: 4),
                      Text(report.country!, style: const TextStyle(color: textMuted, fontSize: 11)),
                    ],
                  ],
                ),
                const SizedBox(height: 12),
                Divider(color: borderCol),
              ],
            ),
            const SizedBox(height: 16),

            // Key metrics cards Row
            Row(
              children: [
                Expanded(
                  child: _buildMetricBlock(
                    'COMPANIES HIRING',
                    '${stats?.companies ?? report.companies?.length ?? 12}',
                    'Active Employers',
                    const Color(0xFF10B981),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _buildMetricBlock(
                    'GROWTH TREND',
                    '+${stats?.growth ?? 14}%',
                    'Above avg velocity',
                    textMuted,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _buildMetricBlock(
                    'PLACEMENTS',
                    '${jobs.length}',
                    'Active Positions',
                    accentBlue,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Visual Chart Data
            if (chartData.isNotEmpty) ...[
              Card(
                color: cardBg,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(20),
                  side: BorderSide(color: borderCol),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(18.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: const [
                          Icon(Icons.trending_up, color: accentBlue, size: 20),
                          SizedBox(width: 8),
                          Text(
                            'Job Demand Velocity',
                            style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15, color: Colors.white),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),

                      // Dynamic Bar Charts
                      Column(
                        children: chartData.map((data) {
                          final demandValue = data.demand ?? 50.0;
                          return Padding(
                            padding: const EdgeInsets.only(bottom: 12.0),
                            child: Row(
                              children: [
                                SizedBox(
                                  width: 100,
                                  child: Text(
                                    data.name ?? 'Group',
                                    style: const TextStyle(color: textMuted, fontSize: 11),
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                ),
                                const SizedBox(width: 8),
                                Expanded(
                                  child: Container(
                                    height: 14,
                                    decoration: BoxDecoration(
                                      color: const Color(0xFF0F172A),
                                      borderRadius: BorderRadius.circular(100),
                                    ),
                                    child: ClipRRect(
                                      borderRadius: BorderRadius.circular(100),
                                      child: LinearProgressIndicator(
                                        value: (demandValue / 100.0).clamp(0.0, 1.0),
                                        backgroundColor: Colors.transparent,
                                        valueColor: const AlwaysStoppedAnimation(Color(0xFF8B5CF6)),
                                      ),
                                    ),
                                  ),
                                ),
                                const SizedBox(width: 8),
                                Text(
                                  '${demandValue.toInt()}%',
                                  style: const TextStyle(
                                    fontFamily: 'monospace',
                                    fontWeight: FontWeight.bold,
                                    color: Color(0xFF8B5CF6),
                                    fontSize: 11,
                                  ),
                                ),
                              ],
                            ),
                          );
                        }).toList(),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
            ],

            // Distribution donut chart
            if (distribution.isNotEmpty) ...[
              Card(
                color: cardBg,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(20),
                  side: BorderSide(color: borderCol),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(18.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Location Distribution',
                        style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 15),
                      ),
                      const SizedBox(height: 16),

                      Row(
                        children: [
                          // Left donut chart
                          Container(
                            width: 110,
                            height: 110,
                            alignment: Alignment.center,
                            child: CustomPaint(
                              size: const Size(110, 110),
                              painter: DonutChartPainter(
                                distribution: distribution,
                              ),
                              child: const Center(
                                child: Text(
                                  'DIST',
                                  style: TextStyle(fontFamily: 'monospace', fontWeight: FontWeight.bold, color: Colors.white, fontSize: 10),
                                ),
                              ),
                            ),
                          ),
                          const SizedBox(width: 24),

                          // Right Legends list
                          Expanded(
                            child: Column(
                              children: List.generate(distribution.length, (idx) {
                                final item = distribution[idx];
                                final colors = const [Color(0xFF8B5CF6), Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B)];
                                final markerColor = colors[idx % colors.length];

                                return Padding(
                                  padding: const EdgeInsets.only(bottom: 6.0),
                                  child: Row(
                                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                    children: [
                                      Row(
                                        children: [
                                          Container(
                                            width: 8,
                                            height: 8,
                                            decoration: BoxDecoration(color: markerColor, shape: BoxShape.circle),
                                          ),
                                          const SizedBox(width: 6),
                                          Text(
                                            item.name ?? 'City',
                                            style: const TextStyle(color: textMuted, fontSize: 11),
                                          ),
                                        ],
                                      ),
                                      Text(
                                        '${item.value?.toInt() ?? 0}',
                                        style: const TextStyle(fontFamily: 'monospace', color: Colors.white, fontSize: 11),
                                      ),
                                    ],
                                  ),
                                );
                              }),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
            ],

            // Key Insights & Briefing Content block
            Card(
              color: cardBg,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(20),
                side: BorderSide(color: borderCol),
              ),
              child: Padding(
                padding: const EdgeInsets.all(18.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: const [
                        Icon(Icons.auto_awesome, color: Color(0xFFF59E0B), size: 22),
                        SizedBox(width: 8),
                        Text(
                          'Key Insights & Market Analysis',
                          style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 15),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),

                    Text(
                      _cleanHtmlTags(report.content ?? report.excerpt ?? report.summary ?? 'No details available.'),
                      style: const TextStyle(color: Color(0xFFE2E8F0), fontSize: 14, height: 1.6),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 20),

            // Active Placements List Section Header
            const Padding(
              padding: EdgeInsets.only(top: 10.0),
              child: Text(
                'ACTIVE PLACEMENTS FOR THIS BRIEFING',
                style: TextStyle(
                  fontFamily: 'monospace',
                  fontWeight: FontWeight.bold,
                  fontSize: 10,
                  letterSpacing: 1.0,
                  color: textMuted,
                ),
              ),
            ),
            const SizedBox(height: 12),

            // Empty State or List of Live Jobs
            if (jobs.isEmpty) ...[
              Card(
                color: cardBg,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                  side: BorderSide(color: borderCol),
                ),
                child: const Padding(
                  padding: EdgeInsets.all(24.0),
                  child: Center(
                    child: Text(
                      'NO ACTIVE PLACEMENTS TRACKED IN THIS QUARTER',
                      style: TextStyle(fontFamily: 'monospace', color: textMuted, fontSize: 11),
                      textAlign: TextAlign.center,
                    ),
                  ),
                ),
              ),
            ] else ...[
              ...sortedJobs.map((rj) {
                final expired = rj.active == false;
                return Padding(
                  padding: const EdgeInsets.only(bottom: 12.0),
                  child: Card(
                    color: cardBg,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(16),
                      side: BorderSide(color: borderCol),
                    ),
                    child: InkWell(
                      onTap: () {
                        // Locate the local db id or save it
                        provider.fetchJobDetail(rj.id ?? '').then((_) {
                          // Jump to job detail
                          if (provider.allJobs.any((j) => j.remoteId == rj.id)) {
                            final localJ = provider.allJobs.firstWhere((j) => j.remoteId == rj.id);
                            widget.onNavigateToJobDetail(localJ.id);
                          }
                        });
                      },
                      borderRadius: BorderRadius.circular(16),
                      child: Padding(
                        padding: const EdgeInsets.all(16.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Expanded(
                                  child: Text(
                                    rj.title ?? 'Job vacancy',
                                    style: TextStyle(
                                      color: expired ? Colors.white.withOpacity(0.6) : Colors.white,
                                      fontWeight: FontWeight.bold,
                                      fontSize: 15,
                                    ),
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                ),
                                const SizedBox(width: 6),
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                  decoration: BoxDecoration(
                                    color: expired ? const Color(0xFFEF4444).withOpacity(0.15) : const Color(0xFF10B981).withOpacity(0.15),
                                    borderRadius: BorderRadius.circular(4),
                                  ),
                                  child: Text(
                                    expired ? 'EXPIRED' : 'ACTIVE',
                                    style: TextStyle(
                                      color: expired ? const Color(0xFFF87171) : const Color(0xFF34D399),
                                      fontWeight: FontWeight.bold,
                                      fontSize: 8,
                                      fontFamily: 'monospace',
                                    ),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 6),

                            Row(
                              children: [
                                const Icon(Icons.business, color: textMuted, size: 13),
                                const SizedBox(width: 4),
                                Text(
                                  rj.company ?? 'Employer',
                                  style: TextStyle(
                                    color: expired ? Colors.white.withOpacity(0.6) : Colors.white,
                                    fontSize: 12,
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 4),

                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Row(
                                  children: [
                                    const Icon(Icons.place, color: textMuted, size: 13),
                                    const SizedBox(width: 4),
                                    Text(
                                      rj.location ?? 'Worldwide',
                                      style: const TextStyle(color: textMuted, fontSize: 11),
                                    ),
                                  ],
                                ),
                                if (rj.salary != null && rj.salary!.isNotEmpty)
                                  Text(
                                    rj.salary!,
                                    style: const TextStyle(fontFamily: 'monospace', color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.bold),
                                  ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                );
              }).toList(),
            ],
          ],
        ),
      ),
    );
  }

  String _cleanHtmlTags(String html) {
    return html
        .replaceAll(RegExp(r'<[^>]*>'), '')
        .replaceAll('&nbsp;', ' ')
        .replaceAll('&amp;', '&');
  }

  Widget _buildMetricBlock(String title, String value, String subtitle, Color markerColor) {
    return Card(
      color: const Color(0xFF1E293B),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(color: const Color(0xFF334155).withOpacity(0.5)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: const TextStyle(fontFamily: 'monospace', color: textMuted, fontSize: 9, fontWeight: FontWeight.bold),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
            const SizedBox(height: 4),
            Text(
              value,
              style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.black),
            ),
            Text(
              subtitle,
              style: TextStyle(color: markerColor, fontWeight: FontWeight.bold, fontSize: 9),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ),
      ),
    );
  }
}

class DonutChartPainter extends CustomPainter {
  final List<RemoteDistributionItem> distribution;

  DonutChartPainter({required this.distribution});

  @override
  void paint(Canvas canvas, Size size) {
    final double total = distribution.fold(0.0, (sum, item) => sum + (item.value ?? 0.0));
    if (total == 0) return;

    final colors = const [Color(0xFF8B5CF6), Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B)];
    final center = Offset(size.width / 2, size.height / 2);
    final radius = min(size.width / 2, size.height / 2) - 8;

    double startAngle = -pi / 2;

    for (var i = 0; i < distribution.length; i++) {
      final item = distribution[i];
      final sweepAngle = ((item.value ?? 0.0) / total) * 2 * pi;

      final paint = Paint()
        ..color = colors[i % colors.length]
        ..style = PaintingStyle.stroke
        ..strokeWidth = 12;

      canvas.drawArc(
        Rect.fromCircle(center: center, radius: radius),
        startAngle,
        sweepAngle,
        false,
        paint,
      );

      startAngle += sweepAngle;
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}
