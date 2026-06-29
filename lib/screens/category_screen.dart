import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../providers/jobs_provider.dart';
import '../models/models.dart';

class CategoryScreen extends StatefulWidget {
  final String categorySlug;
  final String? countrySlug;

  const CategoryScreen({
    super.key,
    required this.categorySlug,
    this.countrySlug,
  });

  @override
  State<CategoryScreen> createState() => _CategoryScreenState();
}

class _CategoryScreenState extends State<CategoryScreen> {
  int _currentPage = 1;
  static const int _jobsPerPage = 10;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final resolvedCategory = _resolveCategoryName(widget.categorySlug);
      Provider.of<JobsProvider>(context, listen: false).fetchLiveCategoryJobs(resolvedCategory);
    });
  }

  String _resolveCategoryName(String slug) {
    switch (slug.toLowerCase()) {
      case 'engineering':
        return 'Engineering';
      case 'design':
        return 'Design';
      case 'marketing':
        return 'Marketing';
      case 'product-management':
        return 'Product Management';
      case 'writing':
        return 'Writing';
      case 'customer-support':
        return 'Customer Support';
      case 'finance':
        return 'Finance';
      case 'hr':
      case 'human-resources':
        return 'Human Resources';
      default:
        return slug.split('-').map((word) {
          if (word.isEmpty) return '';
          return word[0].toUpperCase() + word.substring(1);
        }).join(' ');
    }
  }

  String _resolveCountryName(String? slug, String selectedCountryState) {
    if (slug != null && slug.isNotEmpty) {
      switch (slug.toLowerCase()) {
        case 'tanzania':
          return 'Tanzania';
        case 'united-states':
          return 'United States';
        case 'united-kingdom':
          return 'United Kingdom';
        case 'germany':
          return 'Germany';
        case 'kenya':
          return 'Kenya';
        case 'south-africa':
          return 'South Africa';
        default:
          return slug.split('-').map((word) {
            if (word.isEmpty) return '';
            return word[0].toUpperCase() + word.substring(1);
          }).join(' ');
      }
    } else if (selectedCountryState != 'Worldwide') {
      return selectedCountryState;
    } else {
      return '';
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<JobsProvider>(context);
    final allJobs = provider.allJobs;
    final isCategoryJobsLoading = provider.isCategoryJobsLoading;

    final categoryName = _resolveCategoryName(widget.categorySlug);
    final countryName = _resolveCountryName(widget.countrySlug, provider.selectedCountry);
    final isWorldwide = countryName.isEmpty;

    final filteredJobs = allJobs.where((job) {
      final matchesCategory = job.category.toLowerCase() == categoryName.toLowerCase();
      final matchesCountry = isWorldwide || job.location.toLowerCase().contains(countryName.toLowerCase());
      return matchesCategory && matchesCountry && job.active;
    }).toList();

    filteredJobs.sort((a, b) => b.datePosted.compareTo(a.datePosted));

    final totalJobs = filteredJobs.length;
    final totalPages = (totalJobs / _jobsPerPage).ceil() == 0 ? 1 : (totalJobs / _jobsPerPage).ceil();
    final pageToUse = _currentPage > totalPages ? totalPages : _currentPage;
    final startIndex = (pageToUse - 1) * _jobsPerPage;
    final endIndex = (startIndex + _jobsPerPage) > totalJobs ? totalJobs : (startIndex + _jobsPerPage);

    final paginatedJobs = (startIndex < totalJobs) ? filteredJobs.subList(startIndex, endIndex) : <JobEntity>[];

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: Text(
          isWorldwide ? '$categoryName Jobs' : '$categoryName Jobs in $countryName',
          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
        ),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(16.0),
          children: [
            // 1. Breadcrumbs
            Row(
              children: [
                InkWell(
                  onTap: () => Navigator.pop(context),
                  child: const Text(
                    'HOME',
                    style: TextStyle(fontFamily: 'monospace', fontSize: 10, fontWeight: FontWeight.bold, color: Color(0xFF64748B)),
                  ),
                ),
                const SizedBox(width: 6),
                const Text('/', style: TextStyle(fontFamily: 'monospace', fontSize: 10, color: Color(0xFF475569))),
                const SizedBox(width: 6),
                const Text(
                  'CATEGORY',
                  style: TextStyle(fontFamily: 'monospace', fontSize: 10, fontWeight: FontWeight.bold, color: Color(0xFF64748B)),
                ),
                const SizedBox(width: 6),
                const Text('/', style: TextStyle(fontFamily: 'monospace', fontSize: 10, color: Color(0xFF475569))),
                const SizedBox(width: 6),
                Text(
                  categoryName.toUpperCase(),
                  style: const TextStyle(fontFamily: 'monospace', fontSize: 10, fontWeight: FontWeight.bold, color: Color(0xFF3B82F6)),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // 2. Header
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        isWorldwide ? '$categoryName Jobs' : '$categoryName Jobs in $countryName',
                        style: const TextStyle(fontSize: 24, fontWeight: FontWeight.black, color: Colors.white),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        isWorldwide
                            ? 'Browse $totalJobs $categoryName jobs worldwide'
                            : 'Browse $totalJobs $categoryName jobs in $countryName',
                        style: const TextStyle(fontSize: 13, color: Color(0xFF94A3B8)),
                      ),
                    ],
                  ),
                ),
                if (isCategoryJobsLoading)
                  Row(
                    children: const [
                      SizedBox(
                        width: 12,
                        height: 12,
                        child: CircularProgressIndicator(color: Color(0xFF3B82F6), strokeWidth: 1.5),
                      ),
                      SizedBox(width: 4),
                      Text('SYNCING...', style: TextStyle(fontFamily: 'monospace', fontSize: 10, fontWeight: FontWeight.bold, color: Color(0xFF3B82F6))),
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
                      const Text('LIVE', style: TextStyle(fontFamily: 'monospace', fontSize: 10, fontWeight: FontWeight.bold, color: Color(0xFF10B981))),
                    ],
                  ),
              ],
            ),
            const SizedBox(height: 16),

            // 3. Ad Banner Component
            Card(
              color: const Color(0xFF1E293B).withOpacity(0.3),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
                side: BorderSide(color: const Color(0xFF3B82F6).withOpacity(0.15)),
              ),
              child: InkWell(
                onTap: () {
                  Navigator.pushNamed(context, '/ai_advisor');
                },
                borderRadius: BorderRadius.circular(16),
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Row(
                    children: [
                      Container(
                        width: 40,
                        height: 40,
                        decoration: BoxDecoration(
                          color: const Color(0xFF2563EB).withOpacity(0.15),
                          shape: BoxShape.circle,
                        ),
                        alignment: Alignment.center,
                        child: const Icon(Icons.auto_awesome, color: Color(0xFF60A5FA), size: 20),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'SPONSORED BY AI ADVISOR',
                              style: TextStyle(fontFamily: 'monospace', fontSize: 9, fontWeight: FontWeight.bold, color: Color(0xFF60A5FA), letterSpacing: 1.0),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              'Ace your next $categoryName interview! Generate custom mock interview questions tailored for this category in seconds.',
                              style: const TextStyle(fontSize: 11, color: Color(0xFF94A3B8), height: 1.4),
                            ),
                            const SizedBox(height: 4),
                            const Text(
                              'Try AI Advisor Now →',
                              style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Color(0xFF3B82F6)),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
            const SizedBox(height: 16),

            // 4. Jobs List
            if (filteredJobs.isEmpty)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 48.0),
                child: Column(
                  children: [
                    Container(
                      width: 64,
                      height: 64,
                      decoration: const BoxDecoration(color: Color(0xFF1E293B), shape: BoxShape.circle),
                      alignment: Alignment.center,
                      child: const Icon(Icons.public, color: Color(0xFF475569), size: 32),
                    ),
                    const SizedBox(height: 16),
                    const Text('No jobs found', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white)),
                    const SizedBox(height: 8),
                    Text(
                      isWorldwide
                          ? 'No $categoryName jobs available right now.'
                          : 'No $categoryName jobs available in $countryName right now.',
                      style: const TextStyle(fontSize: 12, color: Color(0xFF64748B)),
                      textAlign: TextAlign.center,
                    ),
                  ],
                ),
              )
            else
              Column(
                children: paginatedJobs.map((job) => _buildJobItemRow(job)).toList(),
              ),

            // 5. Pagination controls
            if (totalPages > 1)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 16.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    TextButton.icon(
                      onPressed: pageToUse > 1
                          ? () {
                              setState(() {
                                _currentPage = pageToUse - 1;
                              });
                            }
                          : null,
                      icon: const Icon(Icons.chevron_left, size: 16),
                      label: const Text('PREV', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, letterSpacing: 1.0)),
                      style: TextButton.styleFrom(
                        foregroundColor: const Color(0xFF3B82F6),
                        disabledForegroundColor: const Color(0xFF475569),
                      ),
                    ),
                    const SizedBox(width: 16),
                    Text(
                      'Page $pageToUse of $totalPages',
                      style: const TextStyle(color: Color(0xFF64748B), fontSize: 12, fontWeight: FontWeight.medium),
                    ),
                    const SizedBox(width: 16),
                    TextButton.icon(
                      onPressed: pageToUse < totalPages
                          ? () {
                              setState(() {
                                _currentPage = pageToUse + 1;
                              });
                            }
                          : null,
                      icon: const Text('NEXT', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, letterSpacing: 1.0)),
                      label: const Icon(Icons.chevron_right, size: 16),
                      style: TextButton.styleFrom(
                        foregroundColor: const Color(0xFF3B82F6),
                        disabledForegroundColor: const Color(0xFF475569),
                      ),
                    ),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildJobItemRow(JobEntity job) {
    final isUrl = job.logoResName.startsWith('http://') || job.logoResName.startsWith('https://');

    return Padding(
      padding: const EdgeInsets.only(bottom: 12.0),
      child: Card(
        color: const Color(0xFF1E293B).withOpacity(0.15),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: BorderSide(color: Colors.white.withOpacity(0.05)),
        ),
        child: InkWell(
          onTap: () {
            Navigator.pushNamed(context, 'job_detail/${job.id}');
          },
          borderRadius: BorderRadius.circular(16),
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    // Logo
                    Container(
                      width: 40,
                      height: 40,
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(10),
                        color: Colors.white.withOpacity(0.05),
                        border: Border.all(color: Colors.white.withOpacity(0.1)),
                      ),
                      clipBehavior: Clip.antiAlias,
                      alignment: Alignment.center,
                      child: isUrl
                          ? CachedNetworkImage(
                              imageUrl: job.logoResName,
                              fit: BoxFit.cover,
                              errorWidget: (context, url, err) => Text(
                                job.company.substring(0, 1).toUpperCase(),
                                style: const TextStyle(color: Color(0xFF94A3B8), fontWeight: FontWeight.bold),
                              ),
                            )
                          : Text(
                              job.company.isNotEmpty ? job.company.substring(0, 1).toUpperCase() : '?',
                              style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 18, fontWeight: FontWeight.bold),
                            ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                decoration: BoxDecoration(
                                  color: const Color(0xFF3B82F6).withOpacity(0.12),
                                  borderRadius: BorderRadius.circular(4),
                                ),
                                child: Text(
                                  (job.type.isNotEmpty ? job.type : 'General').toUpperCase(),
                                  style: const TextStyle(color: Color(0xFF60A5FA), fontSize: 8, fontWeight: FontWeight.bold),
                                ),
                              ),
                              if (job.workplace.isNotEmpty) ...[
                                const SizedBox(width: 6),
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                  decoration: BoxDecoration(
                                    color: const Color(0xFF10B981).withOpacity(0.12),
                                    borderRadius: BorderRadius.circular(4),
                                  ),
                                  child: Text(
                                    job.workplace.toUpperCase(),
                                    style: const TextStyle(color: Color(0xFF34D399), fontSize: 8, fontWeight: FontWeight.bold),
                                  ),
                                ),
                              ],
                            ],
                          ),
                          const SizedBox(height: 2),
                          Text(
                            job.title,
                            style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: Row(
                        children: [
                          const Icon(Icons.business, color: Color(0xFF64748B), size: 12),
                          const SizedBox(width: 4),
                          Expanded(
                            child: Text(
                              job.company,
                              style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 11),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Row(
                        children: [
                          const Icon(Icons.place, color: Color(0xFF64748B), size: 12),
                          const SizedBox(width: 4),
                          Expanded(
                            child: Text(
                              job.location.isNotEmpty ? job.location : 'Remote',
                              style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 11),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                if (job.salary.isNotEmpty) ...[
                  const SizedBox(height: 10),
                  Text(
                    job.salary,
                    style: const TextStyle(fontFamily: 'monospace', color: Color(0xFF34D399), fontWeight: FontWeight.bold, fontSize: 10),
                  ),
                ],
              ],
            ),
          ),
        ),
      ),
    );
  }
}
