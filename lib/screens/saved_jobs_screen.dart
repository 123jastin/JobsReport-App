import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';
import '../providers/jobs_provider.dart';
import '../models/models.dart';

class SavedJobsScreen extends StatefulWidget {
  const SavedJobsScreen({super.key});

  @override
  State<SavedJobsScreen> createState() => _SavedJobsScreenState();
}

class _SavedJobsScreenState extends State<SavedJobsScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _launchUrl(String url) async {
    final Uri uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<JobsProvider>(context);
    final bookmarked = provider.allJobs.where((j) => j.isBookmarked).toList();
    final applied = provider.allJobs.where((j) => j.isApplied).toList();

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: const Text('My Career Board', style: TextStyle(fontWeight: FontWeight.bold)),
        bottom: TabBar(
          controller: _tabController,
          indicatorColor: const Color(0xFF3B82F6),
          labelColor: const Color(0xFF3B82F6),
          unselectedLabelColor: const Color(0xFF94A3B8),
          tabs: [
            Tab(text: 'Bookmarked (${bookmarked.length})'),
            Tab(text: 'Applied Tracker (${applied.length})'),
          ],
        ),
      ),
      body: Column(
        children: [
          Expanded(
            child: TabBarView(
              controller: _tabController,
              children: [
                // Bookmarked tab
                _buildBookmarkedList(bookmarked, provider),

                // Applied Tracker tab
                _buildAppliedTrackerList(applied, provider),
              ],
            ),
          ),

          // Post Job Button
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: ElevatedButton.icon(
              onPressed: () => _launchUrl('https://jobsreport.online/post-job'),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFFEF4444),
                foregroundColor: Colors.white,
                minimumSize: const Size(double.infinity, 48),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              ),
              icon: const Icon(Icons.add, size: 18),
              label: const Text(
                'Post Job',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBookmarkedList(List<JobEntity> list, JobsProvider provider) {
    if (list.isEmpty) {
      return _buildEmptyState(
        Icons.work_outline,
        'No Saved Jobs',
        'Jobs you bookmark from the main board will appear here for easy offline tracking.',
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.only(bottom: 16),
      itemCount: list.length,
      itemBuilder: (context, idx) {
        final job = list[idx];

        return Card(
          color: const Color(0xFF1E293B),
          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          child: ListTile(
            contentPadding: const EdgeInsets.all(16),
            onTap: () {
              Navigator.pushNamed(context, 'job_detail/${job.id}');
            },
            title: Text(
              job.title,
              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
            ),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SizedBox(height: 2),
                Text(
                  '${job.company} • ${job.location}',
                  style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 13),
                ),
                const SizedBox(height: 4),
                Text(
                  'Salary: ${job.salary}',
                  style: const TextStyle(color: Color(0xFF10B981), fontSize: 12, fontWeight: FontWeight.bold),
                ),
              ],
            ),
            trailing: IconButton(
              icon: const Icon(Icons.bookmark_remove, color: Color(0xFFEF4444)),
              onPressed: () {
                provider.toggleBookmark(job.remoteId);
              },
            ),
          ),
        );
      },
    );
  }

  Widget _buildAppliedTrackerList(List<JobEntity> list, JobsProvider provider) {
    if (list.isEmpty) {
      return _buildEmptyState(
        Icons.check_circle_outline,
        'No Applications Yet',
        'When you click \'Quick Apply\' on any position, we will move them here to organize your hiring stages.',
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.only(bottom: 16),
      itemCount: list.length,
      itemBuilder: (context, idx) {
        final job = list[idx];

        // Pipeline status colors
        final Color badgeBg;
        final Color badgeText;
        switch (job.status) {
          case 'Interviewing':
            badgeBg = const Color(0xFFFEF3C7);
            badgeText = const Color(0xFFD97706);
            break;
          case 'Offered':
            badgeBg = const Color(0xFFD1FAE5);
            badgeText = const Color(0xFF059669);
            break;
          case 'Rejected':
            badgeBg = const Color(0xFFFEE2E2);
            badgeText = const Color(0xFFDC2626);
            break;
          default: // Applied
            badgeBg = const Color(0xFFDBEAFE);
            badgeText = const Color(0xFF2563EB);
        }

        return Card(
          color: const Color(0xFF1E293B),
          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Expanded(
                      child: InkWell(
                        onTap: () {
                          Navigator.pushNamed(context, 'job_detail/${job.id}');
                        },
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              job.title,
                              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 15),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              job.company,
                              style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 13),
                            ),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),

                    // Status Badge
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(color: badgeBg, borderRadius: BorderRadius.circular(100)),
                      child: Text(
                        job.status,
                        style: TextStyle(color: badgeText, fontWeight: FontWeight.bold, fontSize: 11),
                      ),
                    ),
                  ],
                ),

                if (job.notes.isNotEmpty) ...[
                  const SizedBox(height: 10),
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: const Color(0xFF0F172A).withOpacity(0.5),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      '✍️ Note: ${job.notes}',
                      style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12),
                    ),
                  ),
                ],

                const SizedBox(height: 12),
                const Divider(color: Color(0xFF334155)),
                const SizedBox(height: 10),

                // Interactive Stages
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      'Hiring Stage:',
                      style: TextStyle(color: Color(0xFF64748B), fontWeight: FontWeight.bold, fontSize: 11),
                    ),
                    Wrap(
                      spacing: 4,
                      children: ['Applied', 'Interviewing', 'Offered', 'Rejected'].map((stage) {
                        final isSelected = job.status == stage;
                        final Color chipColor;
                        switch (stage) {
                          case 'Interviewing':
                            chipColor = const Color(0xFFF59E0B);
                            break;
                          case 'Offered':
                            chipColor = const Color(0xFF10B981);
                            break;
                          case 'Rejected':
                            chipColor = const Color(0xFFEF4444);
                            break;
                          default:
                            chipColor = const Color(0xFF3B82F6);
                        }

                        return ChoiceChip(
                          label: Text(stage, style: const TextStyle(fontSize: 10)),
                          selected: isSelected,
                          onSelected: (selected) {
                            if (selected) {
                              provider.updateJobStatus(job.remoteId, stage);
                            }
                          },
                          selectedColor: chipColor,
                          disabledColor: const Color(0xFF1E293B),
                          labelStyle: TextStyle(color: isSelected ? Colors.white : const Color(0xFF94A3B8)),
                          backgroundColor: const Color(0xFF0F172A),
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(100)),
                        );
                      }).toList(),
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildEmptyState(IconData icon, String title, String subtitle) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, size: 64, color: const Color(0xFF475569)),
            const SizedBox(height: 16),
            Text(
              title,
              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 18),
            ),
            const SizedBox(height: 8),
            Text(
              subtitle,
              style: const TextStyle(color: Color(0xFF64748B), fontSize: 13),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}
