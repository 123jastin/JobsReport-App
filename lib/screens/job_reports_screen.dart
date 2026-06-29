import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/jobs_provider.dart';
import '../models/models.dart';

class ReportItem {
  final String title;
  final String author;
  final String date;
  final String category;
  final String summary;
  final String country;
  final String role;
  final List<MapEntry<String, double>> metrics;
  final String slug;

  ReportItem({
    required this.title,
    required this.author,
    required this.date,
    required this.category,
    required this.summary,
    required this.country,
    required this.role,
    required this.metrics,
    required this.slug,
  });
}

class JobReportsScreen extends StatefulWidget {
  final Function(String) onNavigateToReportDetail;

  const JobReportsScreen({
    super.key,
    required this.onNavigateToReportDetail,
  });

  @override
  State<JobReportsScreen> createState() => _JobReportsScreenState();
}

class _JobReportsScreenState extends State<JobReportsScreen> {
  final TextEditingController _searchController = TextEditingController();
  String _searchTerm = '';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<JobsProvider>(context, listen: false).fetchReports();
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<JobsProvider>(context);
    final liveReports = provider.liveReports;
    final isReportsLoading = provider.isReportsLoading;
    final selectedCountry = provider.selectedCountry;

    final List<ReportItem> reports = liveReports.map((lr) {
      final List<MapEntry<String, double>> mappedMetrics = [];
      if (lr.metrics != null) {
        for (final m in lr.metrics!) {
          mappedMetrics.add(MapEntry(m.label ?? 'Demand', m.value ?? 0.5));
        }
      }
      return ReportItem(
        title: lr.title ?? 'Market Report',
        author: lr.author ?? 'JobsReport Editorial Team',
        date: lr.monthYear ?? lr.date ?? 'Recent',
        category: lr.category ?? 'Intelligence',
        summary: lr.excerpt ?? lr.summary ?? 'Live telemetry report.',
        country: lr.country ?? 'Worldwide',
        role: lr.role ?? 'General',
        metrics: mappedMetrics,
        slug: lr.slug ?? '',
      );
    }).toList();

    final List<MapEntry<String, String>> countriesList = const [
      MapEntry('Worldwide', '🌍'),
      MapEntry('Tanzania', '🇹🇿'),
      MapEntry('United States', '🇺🇸'),
      MapEntry('United Kingdom', '🇬🇧'),
      MapEntry('Germany', '🇩🇪'),
      MapEntry('Kenya', '🇰🇪'),
      MapEntry('South Africa', '🇿🇦'),
    ];

    final filteredReports = reports.where((report) {
      final matchesSearch = _searchTerm.isEmpty ||
          report.title.toLowerCase().contains(_searchTerm.toLowerCase()) ||
          report.role.toLowerCase().contains(_searchTerm.toLowerCase()) ||
          report.summary.toLowerCase().contains(_searchTerm.toLowerCase()) ||
          report.category.toLowerCase().contains(_searchTerm.toLowerCase());

      final matchesCountry = selectedCountry == 'Worldwide' ||
          report.country.toLowerCase() == selectedCountry.toLowerCase();

      return matchesSearch && matchesCountry;
    }).toList();

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: const Text('Reports & Intelligence', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(16.0),
          children: [
            // Sync Indicator & Hero Header
            Card(
              color: const Color(0xFF1E293B),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(24),
                side: BorderSide(color: const Color(0xFF334155).withOpacity(0.5)),
              ),
              child: Padding(
                padding: const EdgeInsets.all(20.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
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
                              'MARKET INTELLIGENCE ARCHIVES',
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
                        if (isReportsLoading)
                          Row(
                            children: const [
                              SizedBox(
                                width: 10,
                                height: 10,
                                child: CircularProgressIndicator(color: Color(0xFF10B981), strokeWidth: 1.5),
                              ),
                              SizedBox(width: 6),
                              Text('SYNCING...', style: TextStyle(fontFamily: 'monospace', fontSize: 9, fontWeight: FontWeight.bold, color: Color(0xFF94A3B8))),
                            ],
                          )
                        else
                          Row(
                            children: [
                              Container(
                                width: 6,
                                height: 6,
                                decoration: const BoxDecoration(color: Color(0xFF10B981), shape: BoxShape.circle),
                              ),
                              const SizedBox(width: 4),
                              const Text('LIVE SYNC', style: TextStyle(fontFamily: 'monospace', fontSize: 9, fontWeight: FontWeight.bold, color: Color(0xFF10B981))),
                            ],
                          ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Text(
                      selectedCountry == 'Worldwide'
                          ? 'Job Market Reports & Hiring Trend Analysis'
                          : 'Job Market Reports in $selectedCountry',
                      style: const TextStyle(color: Colors.white, fontSize: 22, fontWeight: FontWeight.black, height: 1.2),
                    ),
                    const SizedBox(height: 8),
                    const Text(
                      'JobsReport publishes employment reports, hiring trend analysis, salary intelligence, and labor market insights from active employer telemetry databases globally.',
                      style: TextStyle(color: Color(0xFF94A3B8), fontSize: 13, height: 1.4),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Search & Filter controls
            Card(
              color: const Color(0xFF1E293B),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
                side: BorderSide(color: const Color(0xFF334155).withOpacity(0.5)),
              ),
              child: Padding(
                padding: const EdgeInsets.all(14.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    TextField(
                      controller: _searchController,
                      onChanged: (val) {
                        setState(() {
                          _searchTerm = val;
                        });
                      },
                      style: const TextStyle(color: Colors.white, fontSize: 13),
                      decoration: InputDecoration(
                        hintText: 'Search reports, roles, industries...',
                        hintStyle: const TextStyle(color: Color(0xFF64748B), fontSize: 12),
                        prefixIcon: const Icon(Icons.search, color: Color(0xFF94A3B8)),
                        suffixIcon: _searchTerm.isNotEmpty
                            ? IconButton(
                                icon: const Icon(Icons.clear, color: Color(0xFF94A3B8)),
                                onPressed: () {
                                  _searchController.clear();
                                  setState(() {
                                    _searchTerm = '';
                                  });
                                },
                              )
                            : null,
                        filled: true,
                        fillColor: const Color(0xFF0F172A),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: const BorderSide(color: Color(0xFF3B82F6)),
                        ),
                        enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: const BorderSide(color: Color(0xFF334155)),
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),

                    // Horizontal Country Filters Row
                    const Text(
                      'COUNTRY FILTER:',
                      style: TextStyle(
                        fontFamily: 'monospace',
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                        color: Color(0xFF94A3B8),
                      ),
                    ),
                    const SizedBox(height: 8),

                    SizedBox(
                      height: 38,
                      child: ListView.builder(
                        scrollDirection: Axis.horizontal,
                        itemCount: countriesList.length,
                        itemBuilder: (context, idx) {
                          final item = countriesList[idx];
                          final isSelected = selectedCountry == item.key;
                          return Padding(
                            padding: const EdgeInsets.only(right: 8.0),
                            child: InkWell(
                              onTap: () {
                                provider.setSelectedCountry(item.key);
                              },
                              borderRadius: BorderRadius.circular(8),
                              child: Container(
                                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                                decoration: BoxDecoration(
                                  color: isSelected ? const Color(0xFF3B82F6) : const Color(0xFF0F172A),
                                  borderRadius: BorderRadius.circular(8),
                                  border: isSelected ? null : Border.all(color: const Color(0xFF334155)),
                                ),
                                child: Row(
                                  children: [
                                    Text(item.value, style: const TextStyle(fontSize: 14)),
                                    const SizedBox(width: 6),
                                    Text(
                                      item.key,
                                      style: TextStyle(
                                        color: isSelected ? Colors.white : const Color(0xFF94A3B8),
                                        fontWeight: FontWeight.bold,
                                        fontSize: 11,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            ),
                          );
                        },
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Highlights Section (Only when searching or filter is empty)
            if (_searchTerm.isEmpty) ...[
              const Padding(
                padding: EdgeInsets.only(left: 4.0),
                child: Text(
                  'MARKET HIGHLIGHTS',
                  style: TextStyle(
                    fontFamily: 'monospace',
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                    color: Color(0xFF64748B),
                    letterSpacing: 1.0,
                  ),
                ),
              ),
              const SizedBox(height: 10),

              Row(
                children: [
                  Expanded(
                    child: Card(
                      color: const Color(0xFF064E3B).withOpacity(0.2),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                        side: BorderSide(color: const Color(0xFF10B981).withOpacity(0.2)),
                      ),
                      child: Padding(
                        padding: const EdgeInsets.all(12.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: const [
                            Text(
                              '+45%',
                              style: TextStyle(fontFamily: 'monospace', fontWeight: FontWeight.black, color: Color(0xFF10B981), fontSize: 16),
                            ),
                            SizedBox(height: 4),
                            Text(
                              'AI Specialist',
                              style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white, fontSize: 12),
                            ),
                            Text(
                              'Neural integration & LLM demand spikes.',
                              style: TextStyle(color: Color(0xFF94A3B8), fontSize: 10, height: 1.3),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Card(
                      color: const Color(0xFF1E3A8A).withOpacity(0.2),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                        side: BorderSide(color: const Color(0xFF3B82F6).withOpacity(0.2)),
                      ),
                      child: Padding(
                        padding: const EdgeInsets.all(12.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: const [
                            Text(
                              '+28%',
                              style: TextStyle(fontFamily: 'monospace', fontWeight: FontWeight.black, color: Color(0xFF3B82F6), fontSize: 16),
                            ),
                            SizedBox(height: 4),
                            Text(
                              'Fintech Surge',
                              style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white, fontSize: 12),
                            ),
                            Text(
                              'Mobile money & regional banking gateways.',
                              style: TextStyle(color: Color(0xFF94A3B8), fontSize: 10, height: 1.3),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
            ],

            // List of filtered reports
            if (filteredReports.isEmpty) ...[
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 40.0),
                child: Column(
                  children: [
                    const Icon(Icons.info, size: 48, color: Color(0xFF64748B)),
                    const SizedBox(height: 12),
                    const Text('No Matching Reports Found', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16)),
                    const SizedBox(height: 6),
                    const Padding(
                      padding: EdgeInsets.symmetric(horizontal: 24.0),
                      child: Text(
                        'Try adjusting your search query, or clear filters to see all available market telemetry.',
                        style: TextStyle(color: Color(0xFF64748B), fontSize: 12),
                        textAlign: Center,
                      ),
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: () {
                        setState(() {
                          _searchTerm = '';
                        });
                        provider.setSelectedCountry('Worldwide');
                      },
                      style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF3B82F6)),
                      child: const Text('Reset Filters', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                    ),
                  ],
                ),
              ),
            ] else ...[
              ...filteredReports.map((report) {
                return Padding(
                  padding: const EdgeInsets.only(bottom: 16.0),
                  child: Card(
                    color: const Color(0xFF1E293B),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(16),
                      side: BorderSide(color: const Color(0xFF334155).withOpacity(0.5)),
                    ),
                    child: InkWell(
                      onTap: () => widget.onNavigateToReportDetail(report.slug),
                      borderRadius: BorderRadius.circular(16),
                      child: Padding(
                        padding: const EdgeInsets.all(18.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            // Header metadata
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                  decoration: BoxDecoration(
                                    color: const Color(0xFF3B82F6).withOpacity(0.15),
                                    borderRadius: BorderRadius.circular(6),
                                  ),
                                  child: Text(
                                    report.category.toUpperCase(),
                                    style: const TextStyle(
                                      color: Color(0xFF60A5FA),
                                      fontFamily: 'monospace',
                                      fontSize: 10,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                ),
                                Row(
                                  children: [
                                    const Icon(Icons.date_range, color: Color(0xFF64748B), size: 12),
                                    const SizedBox(width: 6),
                                    Text(
                                      report.date,
                                      style: const TextStyle(color: Color(0xFF64748B), fontSize: 11),
                                    ),
                                  ],
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),

                            Text(
                              report.title,
                              style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.black, height: 1.3),
                            ),
                            const SizedBox(height: 6),

                            Row(
                              children: [
                                const Icon(Icons.person, color: Color(0xFF94A3B8), size: 12),
                                const SizedBox(width: 6),
                                Text(
                                  'By ${report.author}',
                                  style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 11),
                                ),
                                const SizedBox(width: 8),
                                const Text('•', style: TextStyle(color: Color(0xFF64748B), fontSize: 11)),
                                const SizedBox(width: 8),
                                const Icon(Icons.place, color: Color(0xFF94A3B8), size: 12),
                                const SizedBox(width: 6),
                                Text(
                                  report.country,
                                  style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 11),
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),

                            Text(
                              report.summary,
                              style: const TextStyle(color: Color(0xFFE2E8F0), fontSize: 13, height: 1.4),
                            ),

                            if (report.metrics.isNotEmpty) ...[
                              const SizedBox(height: 16),
                              Divider(color: const Color(0xFF334155).withOpacity(0.5)),
                              const SizedBox(height: 12),
                              const Text(
                                'DEMAND INDEX METRICS',
                                style: TextStyle(
                                  fontFamily: 'monospace',
                                  fontSize: 10,
                                  fontWeight: FontWeight.bold,
                                  color: Color(0xFF64748B),
                                ),
                              ),
                              const SizedBox(height: 10),

                              // Dynamic bar charts
                              Column(
                                children: report.metrics.map((metric) {
                                  return Padding(
                                    padding: const EdgeInsets.only(bottom: 8.0),
                                    child: Row(
                                      children: [
                                        SizedBox(
                                          width: 110,
                                          child: Text(
                                            metric.key,
                                            style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 11),
                                            maxLines: 1,
                                            overflow: TextOverflow.ellipsis,
                                          ),
                                        ),
                                        Expanded(
                                          child: Container(
                                            height: 10,
                                            decoration: BoxDecoration(
                                              color: const Color(0xFF334155),
                                              borderRadius: BorderRadius.circular(100),
                                            ),
                                            child: ClipRRect(
                                              borderRadius: BorderRadius.circular(100),
                                              child: LinearProgressIndicator(
                                                value: metric.value,
                                                backgroundColor: Colors.transparent,
                                                valueColor: const AlwaysStoppedAnimation(Color(0xFF10B981)),
                                              ),
                                            ),
                                          ),
                                        ),
                                        const SizedBox(width: 10),
                                        Text(
                                          '${(metric.value * 100).toInt()}%',
                                          style: const TextStyle(
                                            fontFamily: 'monospace',
                                            fontWeight: FontWeight.bold,
                                            color: Color(0xFF10B981),
                                            fontSize: 11,
                                          ),
                                        ),
                                      ],
                                    ),
                                  );
                                }).toList(),
                              ),
                            ],
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
}
