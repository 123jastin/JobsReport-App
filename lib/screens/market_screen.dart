import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:url_launcher/url_launcher.dart';
import '../providers/jobs_provider.dart';
import '../models/models.dart';

class MarketScreen extends StatefulWidget {
  const MarketScreen({super.key});

  @override
  State<MarketScreen> createState() => _MarketScreenState();
}

class _MarketScreenState extends State<MarketScreen> {
  int _currentPage = 1;
  static const int _jobsPerPage = 6;

  final List<String> _categories = [
    'All',
    'Engineering',
    'Design',
    'Marketing',
    'Product Management',
    'Writing',
    'Customer Support',
    'Finance',
    'Human Resources'
  ];

  final List<String> _workplaces = ['All', 'Remote', 'Hybrid', 'On-site'];
  final List<String> _jobTypes = ['All', 'Full-time', 'Contract', 'Part-time'];

  Future<void> _launchUrl(String url) async {
    final Uri uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<JobsProvider>(context);
    final jobs = provider.filteredJobs;
    final isMarketLoading = provider.isMarketLoading;

    // Mapping countries to flags
    final String currentFlag = whenCountryFlag(provider.selectedCountry);

    // Reset page if bounds change
    final totalJobs = jobs.length;
    final totalPages = (totalJobs / _jobsPerPage).ceil() == 0 ? 1 : (totalJobs / _jobsPerPage).ceil();

    final pageToUse = _currentPage > totalPages ? totalPages : _currentPage;
    final startIndex = (pageToUse - 1) * _jobsPerPage;
    final endIndex = (startIndex + _jobsPerPage) > totalJobs ? totalJobs : (startIndex + _jobsPerPage);

    final paginatedJobs = (startIndex < totalJobs) ? jobs.subList(startIndex, endIndex) : <JobEntity>[];

    final int uniqueCompanies = jobs.map((j) => j.company.toLowerCase()).toSet().length;
    final int uniqueSectors = jobs.map((j) => j.category.toLowerCase()).toSet().length;

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 20.0),
          children: [
            // 1. Header & Dynamic Title
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    const Icon(Icons.trending_up, color: Color(0xFF3B82F6), size: 16),
                    const SizedBox(width: 6),
                    Text(
                      provider.selectedCountry == 'Worldwide'
                          ? 'GLOBAL MARKET TELEMETRY'
                          : '${provider.selectedCountry.toUpperCase()} REGIONAL MARKET TELEMETRY',
                      style: const TextStyle(
                        fontFamily: 'monospace',
                        fontWeight: FontWeight.bold,
                        color: Color(0xFF3B82F6),
                        fontSize: 11,
                        letterSpacing: 2.0,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                Text(
                  provider.selectedCountry == 'Worldwide'
                      ? (provider.selectedCategory != 'All'
                          ? '${provider.selectedCategory} Jobs $currentFlag'
                          : 'Live Job Market $currentFlag')
                      : (provider.selectedCategory != 'All'
                          ? '${provider.selectedCategory} Jobs in ${provider.selectedCountry} $currentFlag'
                          : 'Jobs in ${provider.selectedCountry} $currentFlag'),
                  style: const TextStyle(
                    fontWeight: FontWeight.black,
                    fontSize: 28,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  'Browse $totalJobs active job listings across $uniqueCompanies companies.',
                  style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 14),
                ),
              ],
            ),
            const SizedBox(height: 20),

            // 2. Telemetry Stats Grid
            _buildStatsGrid(totalJobs, paginatedJobs.length, uniqueCompanies, uniqueSectors),
            const SizedBox(height: 20),

            // 3. Search and Quick Filters Control Area
            _buildFiltersCard(provider),
            const SizedBox(height: 20),

            // 4. Stream Header
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    Container(
                      width: 6,
                      height: 6,
                      decoration: const BoxDecoration(color: Color(0xFF10B981), shape: BoxShape.circle),
                    ),
                    const SizedBox(width: 6),
                    Text(
                      'STREAMING $totalJobs VERIFIED SIGNALS',
                      style: const TextStyle(
                        fontFamily: 'monospace',
                        fontWeight: FontWeight.bold,
                        color: Color(0xFF64748B),
                        fontSize: 10,
                        letterSpacing: 1.0,
                      ),
                    ),
                    const SizedBox(width: 8),
                    if (isMarketLoading)
                      Row(
                        children: const [
                          SizedBox(
                            width: 10,
                            height: 10,
                            child: CircularProgressIndicator(color: Color(0xFF3B82F6), strokeWidth: 1.2),
                          ),
                          SizedBox(width: 4),
                          Text('SYNCING...', style: TextStyle(color: Color(0xFF3B82F6), fontSize: 9, fontWeight: FontWeight.bold, fontFamily: 'monospace')),
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
                          const Text('LIVE SYNC', style: TextStyle(color: Color(0xFF10B981), fontSize: 9, fontWeight: FontWeight.bold, fontFamily: 'monospace')),
                        ],
                      ),
                  ],
                ),
                if (totalPages > 1)
                  Text(
                    'PAGE $pageToUse OF $totalPages',
                    style: const TextStyle(color: Color(0xFF64748B), fontSize: 10, fontFamily: 'monospace', fontWeight: FontWeight.bold),
                  ),
              ],
            ),
            const SizedBox(height: 12),

            // 5. Paginated Job Cards List or Empty State
            if (paginatedJobs.isEmpty)
              _buildEmptyState(provider)
            else
              Column(
                children: paginatedJobs.map((job) => _buildJobCardItem(job, provider)).toList(),
              ),

            // 6. See More Jobs Button CTA
            if (pageToUse < totalPages)
              Padding(
                padding: const EdgeInsets.only(top: 12.0),
                child: InkWell(
                  onTap: () {
                    setState(() {
                      _currentPage++;
                    });
                  },
                  child: Container(
                    padding: const EdgeInsets.symmetric(vertical: 16),
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        colors: [
                          const Color(0xFF2563EB).withOpacity(0.15),
                          const Color(0xFF7C3AED).withOpacity(0.15),
                        ],
                      ),
                      borderRadius: BorderRadius.circular(16),
                      border: Border.all(color: const Color(0xFF3B82F6).withOpacity(0.3)),
                    ),
                    alignment: Alignment.center,
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: const [
                        Text(
                          'SEE MORE JOBS',
                          style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 13, letterSpacing: 1.2),
                        ),
                        SizedBox(width: 8),
                        Icon(Icons.keyboard_arrow_right, color: Colors.white, size: 18),
                      ],
                    ),
                  ),
                ),
              ),

            // 7. Core Pagination Row
            if (totalPages > 1) _buildPaginationControls(pageToUse, totalPages),
            const SizedBox(height: 20),

            // 8. Admin Ingest Promo Card
            _buildAdminPromoCard(),
          ],
        ),
      ),
    );
  }

  String whenCountryFlag(String country) {
    switch (country) {
      case 'Tanzania':
        return '🇹🇿';
      case 'United States':
        return '🇺🇸';
      case 'United Kingdom':
        return '🇬🇧';
      case 'Germany':
        return '🇩🇪';
      case 'Kenya':
        return '🇰🇪';
      case 'South Africa':
        return '🇿🇦';
      default:
        return '🌍';
    }
  }

  Widget _buildStatsGrid(int totalJobs, int visibleJobs, int uniqueCompanies, int uniqueSectors) {
    return Column(
      children: [
        Row(
          children: [
            _buildStatCard('ACTIVE SIGNALS', '$totalJobs', '$visibleJobs on page', const Color(0xFF64748B)),
            const SizedBox(width: 10),
            _buildStatCard('HIRING ENTITIES', '$uniqueCompanies', 'Verified employers', const Color(0xFF64748B)),
          ],
        ),
        const SizedBox(height: 10),
        Row(
          children: [
            _buildStatCard('MARKET SECTORS', '$uniqueSectors', 'Active categories', const Color(0xFF64748B)),
            const SizedBox(width: 10),
            _buildStatCard('SIGNAL INTEGRITY', '100%', 'Latency < 25ms', const Color(0xFF10B981)),
          ],
        ),
      ],
    );
  }

  Widget _buildStatCard(String label, String value, String sub, Color valColor) {
    return Expanded(
      child: Card(
        color: const Color(0xFF1E293B).withOpacity(0.4),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: BorderSide(color: const Color(0xFF334155).withOpacity(0.3)),
        ),
        child: Padding(
          padding: const EdgeInsets.all(14.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: const TextStyle(
                  fontFamily: 'monospace',
                  fontSize: 10,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF64748B),
                  letterSpacing: 1.0,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                value,
                style: TextStyle(
                  fontFamily: 'monospace',
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: valColor == const Color(0xFF10B981) ? const Color(0xFF10B981) : Colors.white,
                ),
              ),
              const SizedBox(height: 2),
              Text(
                sub,
                style: const TextStyle(color: Color(0xFF475569), fontSize: 10),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildFiltersCard(JobsProvider provider) {
    return Card(
      color: const Color(0xFF1E293B).withOpacity(0.3),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(24),
        side: BorderSide(color: const Color(0xFF334155).withOpacity(0.4)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Search Input
            TextField(
              controller: TextEditingController(text: provider.searchQuery)..selection = TextSelection.collapsed(offset: provider.searchQuery.length),
              onChanged: (val) => provider.setSearchQuery(val),
              style: const TextStyle(color: Colors.white, fontSize: 14),
              decoration: InputDecoration(
                hintText: 'Search title, company, category...',
                hintStyle: const TextStyle(color: Color(0xFF64748B), fontSize: 13),
                prefixIcon: const Icon(Icons.search, color: Color(0xFF64748B)),
                suffixIcon: provider.searchQuery.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.close, color: Colors.white),
                        onPressed: () {
                          provider.setSearchQuery('');
                        },
                      )
                    : null,
                filled: true,
                fillColor: const Color(0xFF0F172A).withOpacity(0.5),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(16),
                  borderSide: const BorderSide(color: Color(0xFF3B82F6), width: 1.5),
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(16),
                  borderSide: const BorderSide(color: Color(0xFF334155)),
                ),
              ),
            ),
            const SizedBox(height: 14),

            // Sector list
            const Text(
              'SECTORS & ROLES',
              style: TextStyle(
                fontFamily: 'monospace',
                fontSize: 10,
                fontWeight: FontWeight.bold,
                color: Color(0xFF64748B),
                letterSpacing: 1.2,
              ),
            ),
            const SizedBox(height: 6),
            SizedBox(
              height: 36,
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                itemCount: _categories.length,
                itemBuilder: (context, idx) {
                  final category = _categories[idx];
                  final isSelected = provider.selectedCategory == category;

                  return Padding(
                    padding: const EdgeInsets.only(right: 8.0),
                    child: InkWell(
                      onTap: () => provider.selectCategory(category),
                      borderRadius: BorderRadius.circular(12),
                      child: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        decoration: BoxDecoration(
                          color: isSelected ? const Color(0xFF2563EB) : const Color(0xFF1E293B),
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(
                            color: isSelected ? const Color(0xFF3B82F6) : const Color(0xFF334155),
                          ),
                        ),
                        alignment: Alignment.center,
                        child: Text(
                          category.toUpperCase(),
                          style: TextStyle(
                            color: isSelected ? Colors.white : const Color(0xFF94A3B8),
                            fontSize: 10,
                            fontWeight: FontWeight.bold,
                            letterSpacing: 0.5,
                          ),
                        ),
                      ),
                    ),
                  );
                },
              ),
            ),
            const SizedBox(height: 14),

            // Workplace & Job type dropdowns
            const Text(
              'WORKPLACE & JOB TYPE',
              style: TextStyle(
                fontFamily: 'monospace',
                fontSize: 10,
                fontWeight: FontWeight.bold,
                color: Color(0xFF64748B),
                letterSpacing: 1.2,
              ),
            ),
            const SizedBox(height: 6),
            Row(
              children: [
                // Workplace select
                Expanded(
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12),
                    decoration: BoxDecoration(
                      color: const Color(0xFF1E293B).withOpacity(0.5),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: const Color(0xFF334155)),
                    ),
                    child: DropdownButtonHideUnderline(
                      child: DropdownButton<String>(
                        value: provider.selectedWorkplace,
                        dropdownColor: const Color(0xFF1E293B),
                        style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold),
                        icon: const Icon(Icons.arrow_drop_down, color: Color(0xFF94A3B8)),
                        isExpanded: true,
                        items: _workplaces.map((wp) {
                          return DropdownMenuItem<String>(
                            value: wp,
                            child: Text('🌍 $wp'),
                          );
                        }).toList(),
                        onChanged: (val) {
                          if (val != null) provider.selectWorkplace(val);
                        },
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 10),

                // Job Type select
                Expanded(
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12),
                    decoration: BoxDecoration(
                      color: const Color(0xFF1E293B).withOpacity(0.5),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: const Color(0xFF334155)),
                    ),
                    child: DropdownButtonHideUnderline(
                      child: DropdownButton<String>(
                        value: provider.selectedJobType,
                        dropdownColor: const Color(0xFF1E293B),
                        style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold),
                        icon: const Icon(Icons.arrow_drop_down, color: Color(0xFF94A3B8)),
                        isExpanded: true,
                        items: _jobTypes.map((jt) {
                          return DropdownMenuItem<String>(
                            value: jt,
                            child: Text('💼 $jt'),
                          );
                        }).toList(),
                        onChanged: (val) {
                          if (val != null) provider.selectJobType(val);
                        },
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildEmptyState(JobsProvider provider) {
    return Card(
      color: const Color(0xFF1E293B).withOpacity(0.15),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(32),
        side: BorderSide(color: const Color(0xFF334155).withOpacity(0.5)),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 40.0),
        child: Column(
          children: [
            const Icon(Icons.public, color: Color(0xFF475569), size: 48),
            const SizedBox(height: 16),
            const Text(
              'No Active Market Signals Found',
              style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 6),
            const Text(
              'No verified job listings matching your active filters were discovered.',
              style: TextStyle(color: Color(0xFF64748B), fontSize: 12),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: () {
                provider.setSearchQuery('');
                provider.selectCategory('All');
                provider.selectWorkplace('All');
                provider.selectJobType('All');
                provider.setSelectedCountry('Worldwide');
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF1E293B),
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                  side: const BorderSide(color: Color(0xFF334155)),
                ),
              ),
              child: const Text('RESET FILTERS', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12, letterSpacing: 1.0)),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildJobCardItem(JobEntity job, JobsProvider provider) {
    final isUrl = job.logoResName.startsWith('http://') || job.logoResName.startsWith('https://');

    return Padding(
      padding: const EdgeInsets.only(bottom: 12.0),
      child: Card(
        color: const Color(0xFF1E293B).withOpacity(0.15),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(24),
          side: BorderSide(color: const Color(0xFF334155).withOpacity(0.3)),
        ),
        child: InkWell(
          onTap: () {
            Navigator.pushNamed(context, 'job_detail/${job.id}');
          },
          borderRadius: BorderRadius.circular(24),
          child: Padding(
            padding: const EdgeInsets.all(20.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Logo box
                    Container(
                      width: 48,
                      height: 48,
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(14),
                        gradient: const LinearGradient(colors: [Color(0xFF3B82F6), Color(0xFF8B5CF6)]),
                      ),
                      clipBehavior: Clip.antiAlias,
                      alignment: Alignment.center,
                      child: isUrl
                          ? CachedNetworkImage(
                              imageUrl: job.logoResName,
                              fit: BoxFit.cover,
                              errorWidget: (context, url, err) => Text(
                                job.company.substring(0, 2).toUpperCase(),
                                style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                              ),
                            )
                          : Text(
                              job.company.isNotEmpty ? job.company.substring(0, 2).toUpperCase() : 'JR',
                              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                            ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            job.title,
                            style: const TextStyle(color: Colors.white, fontWeight: FontWeight.black, fontSize: 16),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                          Text(
                            job.company,
                            style: const TextStyle(color: Color(0xFF3B82F6), fontWeight: FontWeight.bold, fontSize: 13),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ],
                      ),
                    ),
                    IconButton(
                      icon: Icon(
                        job.isBookmarked ? Icons.bookmark : Icons.bookmark_border,
                        color: job.isBookmarked ? const Color(0xFF3B82F6) : const Color(0xFF64748B),
                        size: 20,
                      ),
                      onPressed: () {
                        provider.toggleBookmark(job.remoteId);
                      },
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                      decoration: BoxDecoration(
                        color: const Color(0xFF10B981).withOpacity(0.1),
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: const Text(
                        'Verified',
                        style: TextStyle(color: Color(0xFF10B981), fontSize: 9, fontWeight: FontWeight.bold),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Text(
                      '${job.workplace} / ${job.type}',
                      style: const TextStyle(color: Color(0xFF64748B), fontSize: 11, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
                const Divider(color: Color(0xFF334155), height: 24),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        const Icon(Icons.place, color: Color(0xFF64748B), size: 12),
                        const SizedBox(width: 4),
                        Text(job.location, style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 11)),
                      ],
                    ),
                    Row(
                      children: [
                        const Icon(Icons.monetization_on, color: Color(0xFF10B981), size: 12),
                        const SizedBox(width: 4),
                        Text(job.salary, style: const TextStyle(color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.bold)),
                      ],
                    ),
                    Row(
                      children: [
                        const Icon(Icons.access_time, color: Color(0xFF64748B), size: 12),
                        const SizedBox(width: 4),
                        Text(job.datePosted, style: const TextStyle(color: Color(0xFF64748B), fontSize: 11)),
                      ],
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildPaginationControls(int page, int total) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          // Previous button
          IconButton(
            icon: Icon(Icons.keyboard_arrow_left, color: page > 1 ? Colors.white : const Color(0xFF64748B)),
            onPressed: page > 1
                ? () {
                    setState(() {
                      _currentPage = page - 1;
                    });
                  }
                : null,
            style: IconButton.styleFrom(
              backgroundColor: const Color(0xFF1E293B),
              side: const BorderSide(color: Color(0xFF334155)),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
          ),

          // Numbers list
          Row(
            children: List.generate(total, (idx) {
              final pNum = idx + 1;
              final isCurrent = page == pNum;

              return Padding(
                padding: const EdgeInsets.symmetric(horizontal: 3.0),
                child: InkWell(
                  onTap: () {
                    setState(() {
                      _currentPage = pNum;
                    });
                  },
                  borderRadius: BorderRadius.circular(10),
                  child: Container(
                    width: 36,
                    height: 36,
                    decoration: BoxDecoration(
                      color: isCurrent ? const Color(0xFF2563EB) : const Color(0xFF1E293B),
                      borderRadius: BorderRadius.circular(10),
                      border: Border.all(color: isCurrent ? const Color(0xFF3B82F6) : const Color(0xFF334155)),
                    ),
                    alignment: Alignment.center,
                    child: Text(
                      '$pNum',
                      style: TextStyle(
                        color: isCurrent ? Colors.white : const Color(0xFF94A3B8),
                        fontWeight: FontWeight.bold,
                        fontSize: 13,
                      ),
                    ),
                  ),
                ),
              );
            }),
          ),

          // Next button
          IconButton(
            icon: Icon(Icons.keyboard_arrow_right, color: page < total ? Colors.white : const Color(0xFF64748B)),
            onPressed: page < total
                ? () {
                    setState(() {
                      _currentPage = page + 1;
                    });
                  }
                : null,
            style: IconButton.styleFrom(
              backgroundColor: const Color(0xFF1E293B),
              side: const BorderSide(color: Color(0xFF334155)),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAdminPromoCard() {
    return Card(
      color: const Color(0xFF1E293B).withOpacity(0.2),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(24),
        side: BorderSide(color: const Color(0xFF3B82F6).withOpacity(0.15)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: const [
                  Text(
                    'Ingest new market signals?',
                    style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 15),
                  ),
                  SizedBox(height: 2),
                  Text(
                    'Access the Admin Studio to add raw market data.',
                    style: TextStyle(color: Color(0xFF94A3B8), fontSize: 11),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),
            ElevatedButton.icon(
              onPressed: () => _launchUrl('https://jobsreport.online/post-job'),
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF10B981),
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
              ),
              icon: const Text('ADMIN', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 11, letterSpacing: 1.0)),
              label: const Icon(Icons.arrow_outward, size: 12),
            ),
          ],
        ),
      ),
    );
  }
}
