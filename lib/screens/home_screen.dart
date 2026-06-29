import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../providers/jobs_provider.dart';
import '../models/models.dart';

class HomeScreen extends StatefulWidget {
  final String? countrySlug;

  const HomeScreen({super.key, this.countrySlug});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  bool _showAllCategories = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final provider = Provider.of<JobsProvider>(context, listen: false);

      // Map country slug if provided in dynamic deep link
      if (widget.countrySlug != null) {
        final String countryName = _mapSlugToCountryName(widget.countrySlug!);
        provider.setSelectedCountry(countryName);
      }

      // Initial Sync
      final String country = provider.selectedCountry;
      provider.fetchHomeData(country);
      provider.fetchCategories();
      provider.fetchMarketData();
    });
  }

  String _mapSlugToCountryName(String slug) {
    switch (slug.toLowerCase().replaceAll('-', ' ')) {
      case 'tanzania':
        return 'Tanzania';
      case 'united states':
        return 'United States';
      case 'united kingdom':
        return 'United Kingdom';
      case 'germany':
        return 'Germany';
      case 'kenya':
        return 'Kenya';
      case 'south africa':
        return 'South Africa';
      default:
        return 'Worldwide';
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<JobsProvider>(context);
    final String selectedCountry = provider.selectedCountry;

    // Flag mapping
    final String currentFlag = _getCountryFlag(selectedCountry);

    // Hardcoded Category definitions to match Kotlin/React exactly
    final List<CategoryMeta> categoryMetadata = [
      CategoryMeta('Engineering', 'engineering', Icons.code, const Color(0xFF3B82F6)),
      CategoryMeta('Design', 'design', Icons.palette, const Color(0xFFEC4899)),
      CategoryMeta('Marketing', 'marketing', Icons.trending_up, const Color(0xFF10B981)),
      CategoryMeta('Product Management', 'product-management', Icons.assessment, const Color(0xFF8B5CF6)),
      CategoryMeta('Writing', 'writing', Icons.edit, const Color(0xFFF59E0B)),
      CategoryMeta('Customer Support', 'customer-support', Icons.headset, const Color(0xFF06B6D4)),
      CategoryMeta('Finance', 'finance', Icons.account_balance, const Color(0xFFE11D48)),
      CategoryMeta('Human Resources', 'hr', Icons.people, const Color(0xFF14B8A6)),
    ];

    // Compute categories with counts dynamically
    final List<CategoryMeta> categoriesWithCounts = provider.liveCategories.isNotEmpty
        ? provider.liveCategories.map((lc) {
            final matchingMeta = categoryMetadata.firstWhere(
              (m) => m.name.toLowerCase() == lc.name.toLowerCase() || m.slug.toLowerCase() == lc.slug?.toLowerCase(),
              orElse: () => CategoryMeta(lc.name, lc.slug ?? '', Icons.work, const Color(0xFF3B82F6)),
            );
            return CategoryMeta(
              lc.name,
              lc.slug ?? lc.name.toLowerCase().replaceAll(' ', '-'),
              matchingMeta.icon,
              matchingMeta.tint,
              jobCount: lc.jobCount ?? lc.jobsCount ?? lc.count ?? provider.allJobs.where((j) => j.category.toLowerCase() == lc.name.toLowerCase()).length,
            );
          }).toList()
        : categoryMetadata.map((meta) {
            final count = provider.allJobs.where((j) => j.category.toLowerCase() == meta.name.toLowerCase()).length;
            return CategoryMeta(meta.name, meta.slug, meta.icon, meta.tint, jobCount: count);
          }).toList();

    final List<CategoryMeta> visibleCategories = _showAllCategories
        ? categoriesWithCounts
        : categoriesWithCounts.take(6).toList();

    // Latest opportunities (first 5 filtered jobs)
    final List<JobEntity> displayJobs = provider.filteredJobs.take(5).toList();

    // Spotlight companies list
    final List<String> spotlightCompanies = provider.liveHomeSpotlight.isNotEmpty
        ? provider.liveHomeSpotlight
        : [
            "SwahiliTech Solutions",
            "NMB Bank Tanzania",
            "NovaTech Solutions",
            "Aether Creative Agency",
          ];

    final List<CountryInfo> countriesList = [
      CountryInfo('Worldwide', '🌍', const Color(0xFF3B82F6)),
      CountryInfo('Tanzania', '🇹🇿', const Color(0xFF10B981)),
      CountryInfo('Kenya', '🇰🇪', const Color(0xFF10B981)),
      CountryInfo('United States', '🇺🇸', const Color(0xFF10B981)),
      CountryInfo('United Kingdom', '🇬🇧', const Color(0xFF10B981)),
      CountryInfo('Germany', '🇩🇪', const Color(0xFF10B981)),
      CountryInfo('South Africa', '🇿🇦', const Color(0xFF10B981)),
    ];

    return Scaffold(
      appBar: AppBar(
        leading: Builder(
          builder: (context) {
            return IconButton(
              icon: const Icon(Icons.menu),
              onPressed: () => Scaffold.of(context).openDrawer(),
            );
          },
        ),
        title: Row(
          children: [
            const Icon(Icons.auto_awesome, color: Color(0xFF3B82F6), size: 20),
            const SizedBox(width: 8),
            Text(
              'JobsReport',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    fontFamily: 'Space Grotesk',
                    fontWeight: FontWeight.black,
                  ),
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: () => Navigator.pushNamed(context, '/dashboard'),
          ),
          IconButton(
            icon: const Icon(Icons.bookmark_outline),
            onPressed: () => Navigator.pushNamed(context, '/saved_jobs'),
          ),
        ],
      ),
      drawer: Drawer(
        backgroundColor: const Color(0xFF0F172A),
        child: Column(
          children: [
            DrawerHeader(
              decoration: const BoxDecoration(
                border: Border(bottom: BorderSide(color: Color(0xFF1E293B))),
              ),
              child: Row(
                children: [
                  const Icon(Icons.auto_awesome, color: Color(0xFF3B82F6), size: 30),
                  const SizedBox(width: 12),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        'JobsReport.online',
                        style: Theme.of(context).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.black,
                              color: Colors.white,
                            ),
                      ),
                      Text(
                        'Hiring Telemetry & Guidance',
                        style: Theme.of(context).textTheme.bodySmall?.copyWith(
                              color: const Color(0xFF94A3B8),
                            ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            Expanded(
              child: ListView(
                padding: EdgeInsets.zero,
                children: [
                  _buildDrawerItem(context, Icons.home, 'Home Dashboard', '/home', isSelected: true),
                  _buildDrawerItem(context, Icons.business_center, 'Browse Vacancies', '/dashboard'),
                  _buildDrawerItem(context, Icons.bar_chart, 'Market Reports', '/reports'),
                  _buildDrawerItem(context, Icons.apartment, 'Top Companies', '/companies'),
                  _buildDrawerItem(context, Icons.map, 'Browse Regions', '/regions'),
                  const Divider(color: Color(0xFF1E293B), height: 32),
                  _buildDrawerItem(context, Icons.psychology, 'AI Advisor', '/ai_advisor'),
                  _buildDrawerItem(context, Icons.bookmark, 'Saved Tracker', '/tracker'),
                  _buildDrawerItem(context, Icons.person, 'My Profile', '/profile'),
                  _buildDrawerItem(context, Icons.admin_panel_settings, 'Admin Studio', '/admin_studio'),
                ],
              ),
            ),
          ],
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // 1. HERO HEADER SECTION
            _buildHeroHeader(provider, selectedCountry, currentFlag),
            const SizedBox(height: 32),

            // 2. EXPLORE JOBS BY COUNTRY SECTION
            _buildCountryExplorer(provider, countriesList, selectedCountry),
            const SizedBox(height: 32),

            // 3. JOB CATEGORIES SECTION
            _buildCategoriesGrid(categoriesWithCounts, visibleCategories, provider.liveCategories.length),
            const SizedBox(height: 32),

            // 4. LATEST OPPORTUNITIES SECTION
            _buildLatestOpportunities(displayJobs, selectedCountry),
            const SizedBox(height: 32),

            // 5. ANALYTICAL REPORTS CARD LINK
            _buildReportsCardLink(context),
            const SizedBox(height: 32),

            // 6. WEEKLY SPOTLIGHT SECTION
            _buildWeeklySpotlight(spotlightCompanies),
            const SizedBox(height: 48),

            // 7. FOOTER SECTION
            _buildFooter(context),
          ],
        ),
      ),
    );
  }

  Widget _buildDrawerItem(BuildContext context, IconData icon, String title, String route, {bool isSelected = false}) {
    return ListTile(
      leading: Icon(icon, color: isSelected ? const Color(0xFF3B82F6) : const Color(0xFF94A3B8)),
      title: Text(
        title,
        style: TextStyle(
          color: isSelected ? Colors.white : const Color(0xFFE2E8F0),
          fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
        ),
      ),
      onTap: () {
        Navigator.pop(context); // close drawer
        if (ModalRoute.of(context)?.settings.name != route) {
          Navigator.pushNamed(context, route);
        }
      },
    );
  }

  Widget _buildHeroHeader(JobsProvider provider, String selectedCountry, String flag) {
    final isSyncing = provider.isHomeDataLoading || provider.isCategoriesLoading || provider.isMarketLoading;

    return Column(
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
                  decoration: const BoxDecoration(
                    color: Color(0xFF3B82F6),
                    shape: BoxShape.circle,
                  ),
                ),
                const SizedBox(width: 8),
                const Text(
                  'REAL-TIME TALENT INTELLIGENCE',
                  style: TextStyle(
                    color: Color(0xFF3B82F6),
                    fontWeight: FontWeight.bold,
                    fontSize: 11,
                    letterSpacing: 1.0,
                  ),
                ),
              ],
            ),
            if (isSyncing)
              Row(
                children: const [
                  SizedBox(
                    width: 10,
                    height: 10,
                    child: CircularProgressIndicator(
                      color: Color(0xFF3B82F6),
                      strokeWidth: 1.2,
                    ),
                  ),
                  SizedBox(width: 6),
                  Text(
                    'SYNCING...',
                    style: TextStyle(color: Color(0xFF94A3B8), fontSize: 9, fontWeight: FontWeight.bold),
                  ),
                ],
              )
            else
              Row(
                children: [
                  Container(
                    width: 6,
                    height: 6,
                    decoration: const BoxDecoration(
                      color: Color(0xFF10B981),
                      shape: BoxShape.circle,
                    ),
                  ),
                  const SizedBox(width: 6),
                  const Text(
                    'LIVE SYNC',
                    style: TextStyle(color: Color(0xFF10B981), fontSize: 9, fontWeight: FontWeight.bold),
                  ),
                ],
              ),
          ],
        ),
        const SizedBox(height: 12),
        Text(
          selectedCountry == 'Worldwide' ? 'Find Your Next\nCareer Opportunity.' : 'Jobs in $selectedCountry\n$flag Latest Vacancies.',
          style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                fontWeight: FontWeight.black,
                color: Colors.white,
                fontSize: 28,
                height: 1.2,
              ),
        ),
        const SizedBox(height: 12),
        Text(
          selectedCountry == 'Worldwide'
              ? 'Insight-first job discovery. We aggregate real-time market data to show you where the demand is actually shifting.'
              : 'Find the latest jobs and career opportunities in $selectedCountry. Browse verified vacancies from top employers hiring in $selectedCountry.',
          style: const TextStyle(
            color: Color(0xFF94A3B8),
            fontSize: 14,
            height: 1.5,
          ),
        ),
        const SizedBox(height: 20),
        Row(
          children: [
            Expanded(
              child: StatBadge(
                icon: Icons.flash_on,
                count: '${provider.liveCategories.length > 0 ? provider.liveCategories.length : 8}',
                label: 'Categories',
                tint: const Color(0xFF3B82F6),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: StatBadge(
                icon: Icons.bar_chart,
                count: '${provider.liveHomeReports.length > 0 ? provider.liveHomeReports.length : 12}',
                label: 'Reports',
                tint: const Color(0xFF10B981),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: StatBadge(
                icon: Icons.business,
                count: '${provider.filteredJobs.length}',
                label: 'Active Jobs',
                tint: const Color(0xFF8B5CF6),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildCountryExplorer(JobsProvider provider, List<CountryInfo> countriesList, String selectedCountry) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Container(
              width: 4,
              height: 16,
              color: const Color(0xFF10B981),
            ),
            const SizedBox(width: 8),
            const Text(
              'Explore Jobs by Country',
              style: TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.bold,
                fontSize: 16,
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        SizedBox(
          height: 40,
          child: ListView.builder(
            scrollDirection: Axis.horizontal,
            itemCount: countriesList.length,
            itemBuilder: (context, index) {
              final country = countriesList[index];
              final isSelected = selectedCountry == country.name;

              return Padding(
                padding: const EdgeInsets.only(right: 8.0),
                child: InkWell(
                  onTap: () {
                    provider.setSelectedCountry(country.name);
                    provider.fetchHomeData(country.name);
                  },
                  borderRadius: BorderRadius.circular(20),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 14.0, vertical: 8.0),
                    decoration: BoxDecoration(
                      color: isSelected ? country.tint.withOpacity(0.2) : const Color(0xFF1E293B),
                      borderRadius: BorderRadius.circular(20),
                      border: Border.all(
                        color: isSelected ? country.tint.withOpacity(0.6) : const Color(0xFF334155).withOpacity(0.4),
                      ),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(country.flag, style: const TextStyle(fontSize: 14)),
                        const SizedBox(width: 6),
                        Text(
                          country.name,
                          style: TextStyle(
                            color: isSelected ? Colors.white : const Color(0xFF94A3B8),
                            fontWeight: FontWeight.bold,
                            fontSize: 12,
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
        const SizedBox(height: 10),
        InkWell(
          onTap: () => Navigator.pushNamed(context, '/regions'),
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 4.0),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: const [
                Icon(Icons.map, color: Color(0xFF10B981), size: 14),
                SizedBox(width: 4),
                Text(
                  'Browse Jobs by City & Region',
                  style: TextStyle(
                    color: Color(0xFF10B981),
                    fontWeight: FontWeight.bold,
                    fontSize: 12,
                  ),
                ),
                SizedBox(width: 2),
                Icon(Icons.chevron_right, color: Color(0xFF10B981), size: 14),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildCategoriesGrid(List<CategoryMeta> categoriesWithCounts, List<CategoryMeta> visibleCategories, int totalCount) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Container(
                      width: 4,
                      height: 16,
                      decoration: const BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.topCenter,
                          end: Alignment.bottomCenter,
                          colors: [Color(0xFF3B82F6), Color(0xFF8B5CF6)],
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    const Text(
                      'Job Categories',
                      style: TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                      ),
                    ),
                  ],
                ),
                Text(
                  _showAllCategories ? 'Showing all $totalCount categories' : 'Top categories',
                  style: const TextStyle(color: Color(0xFF64748B), fontSize: 11, fontFamily: 'monospace'),
                ),
              ],
            ),
            if (categoriesWithCounts.length > 6)
              TextButton.icon(
                onPressed: () {
                  setState(() {
                    _showAllCategories = !_showAllCategories;
                  });
                },
                icon: Text(
                  _showAllCategories ? 'SHOW LESS' : 'SEE MORE',
                  style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Color(0xFF3B82F6)),
                ),
                label: Icon(
                  _showAllCategories ? Icons.expand_less : Icons.expand_more,
                  color: const Color(0xFF3B82F6),
                  size: 16,
                ),
              ),
          ],
        ),
        const SizedBox(height: 12),
        GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 2,
            crossAxisSpacing: 8,
            mainAxisSpacing: 8,
            childAspectRatio: 1.4,
          ),
          itemCount: visibleCategories.length,
          itemBuilder: (context, index) {
            final meta = visibleCategories[index];

            return Card(
              color: const Color(0xFF1E293B).withOpacity(0.4),
              child: InkWell(
                onTap: () {
                  Navigator.pushNamed(context, 'category/${meta.slug}');
                },
                borderRadius: BorderRadius.circular(16),
                child: Padding(
                  padding: const EdgeInsets.all(12.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Container(
                        width: 36,
                        height: 36,
                        decoration: BoxDecoration(
                          color: meta.tint.withOpacity(0.12),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Icon(meta.icon, color: meta.tint, size: 18),
                      ),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            meta.name,
                            style: const TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              fontSize: 12,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                          const SizedBox(height: 2),
                          Text(
                            '${meta.jobCount} active roles',
                            style: const TextStyle(
                              color: Color(0xFF64748B),
                              fontSize: 10,
                              fontFamily: 'monospace',
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            );
          },
        ),
      ],
    );
  }

  Widget _buildLatestOpportunities(List<JobEntity> displayJobs, String selectedCountry) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Container(
                      width: 4,
                      height: 16,
                      color: const Color(0xFF3B82F6),
                    ),
                    const SizedBox(width: 8),
                    const Text(
                      'Latest Opportunities',
                      style: TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                      ),
                    ),
                  ],
                ),
                Text(
                  selectedCountry == 'Worldwide' ? 'Top active job listings globally' : 'Latest vacancies in $selectedCountry',
                  style: const TextStyle(color: Color(0xFF64748B), fontSize: 11),
                ),
              ],
            ),
            TextButton.icon(
              onPressed: () => Navigator.pushNamed(context, '/dashboard'),
              icon: const Text(
                'VIEW ALL',
                style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Color(0xFF3B82F6)),
              ),
              label: const Icon(Icons.arrow_forward, color: Color(0xFF3B82F6), size: 14),
            ),
          ],
        ),
        const SizedBox(height: 12),
        if (displayJobs.isEmpty)
          Card(
            color: const Color(0xFF1E293B).withOpacity(0.3),
            child: Container(
              width: double.infinity,
              padding: const EdgeInsets.all(24.0),
              child: Column(
                children: const [
                  Icon(Icons.work_outline, color: Color(0xFF64748B), size: 40),
                  SizedBox(height: 8),
                  Text(
                    'No opportunities found',
                    style: TextStyle(color: Color(0xFF94A3B8), fontWeight: FontWeight.bold, fontSize: 14),
                  ),
                  Text(
                    'Try switching your location or refining your query.',
                    style: TextStyle(color: Color(0xFF64748B), fontSize: 12),
                  ),
                ],
              ),
            ),
          )
        else
          ListView.separated(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            itemCount: displayJobs.length,
            separatorBuilder: (context, index) => const SizedBox(height: 8),
            itemBuilder: (context, index) {
              final job = displayJobs[index];
              final isUrl = job.logoResName.startsWith('http://') || job.logoResName.startsWith('https://');

              return Card(
                color: const Color(0xFF1E293B),
                child: InkWell(
                  onTap: () {
                    Navigator.pushNamed(context, 'job_detail/${job.remoteId}');
                  },
                  borderRadius: BorderRadius.circular(16),
                  child: Padding(
                    padding: const EdgeInsets.all(14.0),
                    child: Row(
                      children: [
                        Container(
                          width: 40,
                          height: 40,
                          decoration: const BoxDecoration(
                            color: Color(0xFF334155),
                            shape: BoxShape.circle,
                          ),
                          clipBehavior: Clip.antiAlias,
                          alignment: Alignment.center,
                          child: isUrl
                              ? CachedNetworkImage(
                                  imageUrl: job.logoResName,
                                  fit: BoxFit.cover,
                                  placeholder: (context, url) => const CircularProgressIndicator(strokeWidth: 1.5),
                                  errorWidget: (context, url, error) => Text(
                                    job.company.substring(0, 2).toUpperCase(),
                                    style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14),
                                  ),
                                )
                              : Text(
                                  job.company.substring(0, 2).toUpperCase(),
                                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14),
                                ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 6.0, vertical: 2.0),
                                decoration: BoxDecoration(
                                  color: const Color(0xFF3B82F6).withOpacity(0.12),
                                  borderRadius: BorderRadius.circular(4),
                                ),
                                child: Text(
                                  job.category.toUpperCase(),
                                  style: const TextStyle(
                                    color: Color(0xFF60A5FA),
                                    fontSize: 8,
                                    fontWeight: FontWeight.bold,
                                    fontFamily: 'monospace',
                                  ),
                                ),
                              ),
                              const SizedBox(height: 4),
                              Text(
                                job.title,
                                style: const TextStyle(
                                  color: Colors.white,
                                  fontWeight: FontWeight.bold,
                                  fontSize: 14,
                                ),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                              ),
                              const SizedBox(height: 2),
                              Row(
                                children: [
                                  Text(
                                    job.company,
                                    style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12),
                                  ),
                                  const SizedBox(width: 8),
                                  const Text('•', style: TextStyle(color: Color(0xFF64748B))),
                                  const SizedBox(width: 8),
                                  Expanded(
                                    child: Text(
                                      job.location,
                                      style: const TextStyle(color: Color(0xFF64748B), fontSize: 12),
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(width: 8),
                        const Icon(Icons.chevron_right, color: Color(0xFF64748B), size: 20),
                      ],
                    ),
                  ),
                ),
              );
            },
          ),
      ],
    );
  }

  Widget _buildReportsCardLink(BuildContext context) {
    return Card(
      color: const Color(0xFF1E293B),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: const BorderSide(color: Color(0xFF8B5CF6), width: 1.0),
      ),
      child: InkWell(
        onTap: () => Navigator.pushNamed(context, '/reports'),
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(18.0),
          child: Row(
            children: [
              Container(
                width: 46,
                height: 46,
                decoration: BoxDecoration(
                  color: const Color(0xFF8B5CF6).withOpacity(0.15),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Icon(Icons.analytics, color: Color(0xFFA78BFA), size: 24),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: const [
                    Text(
                      'ANALYTICAL REPORTS',
                      style: TextStyle(
                        color: Color(0xFFA78BFA),
                        fontWeight: FontWeight.bold,
                        fontSize: 10,
                        fontFamily: 'monospace',
                      ),
                    ),
                    SizedBox(height: 2),
                    Text(
                      'Market Intelligence Reports',
                      style: TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 14,
                      ),
                    ),
                    Text(
                      'Read detailed analyses and review live skill metrics in East Africa and globally.',
                      style: TextStyle(color: Color(0xFF94A3B8), fontSize: 11),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              const Icon(Icons.arrow_forward, color: Color(0xFFA78BFA), size: 20),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildWeeklySpotlight(List<String> spotlightCompanies) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(20.0),
      decoration: BoxDecoration(
        color: const Color(0xFF1E293B).withOpacity(0.4),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFF334155).withOpacity(0.4)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: const [
                  Icon(Icons.auto_awesome, color: Color(0xFF3B82F6), size: 18),
                  SizedBox(width: 8),
                  Text(
                    'Weekly Spotlight',
                    style: TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      fontSize: 15,
                    ),
                  ),
                ],
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
                decoration: BoxDecoration(
                  color: const Color(0xFF1E293B),
                  borderRadius: BorderRadius.circular(100),
                ),
                child: const Text(
                  'LIVE MARKET DATA',
                  style: TextStyle(
                    color: Color(0xFF64748B),
                    fontWeight: FontWeight.bold,
                    fontSize: 8,
                    fontFamily: 'monospace',
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          const Text(
            'Top companies actively shifting their hiring strategy based on market telemetry.',
            style: TextStyle(color: Color(0xFF94A3B8), fontSize: 13, height: 1.4),
          ),
          const SizedBox(height: 16),
          SizedBox(
            height: 30,
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              itemCount: spotlightCompanies.length,
              itemBuilder: (context, index) {
                final company = spotlightCompanies[index];
                return Padding(
                  padding: const EdgeInsets.only(right: 16.0),
                  child: InkWell(
                    onTap: () {
                      final provider = Provider.of<JobsProvider>(context, listen: false);
                      provider.setSearchQuery(company);
                      Navigator.pushNamed(context, '/dashboard');
                    },
                    child: Text(
                      company.toUpperCase(),
                      style: const TextStyle(
                        color: Color(0xFFCBD5E1),
                        fontWeight: FontWeight.black,
                        fontSize: 11,
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFooter(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Divider(color: Color(0xFF334155), height: 1),
        const SizedBox(height: 20),
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'QUICK LINKS',
                    style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 10),
                  ),
                  const SizedBox(height: 8),
                  FooterLink('About Us', () => Navigator.pushNamed(context, '/about_us')),
                  FooterLink('Contact Us', () => Navigator.pushNamed(context, '/contact_us')),
                  FooterLink('Companies', () => Navigator.pushNamed(context, '/companies')),
                  FooterLink('Job Regions', () => Navigator.pushNamed(context, '/regions')),
                ],
              ),
            ),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'LEGAL',
                    style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 10),
                  ),
                  const SizedBox(height: 8),
                  FooterLink('Privacy Policy', () => Navigator.pushNamed(context, '/privacy_policy')),
                  FooterLink('Terms of Service', () => Navigator.pushNamed(context, '/terms_of_service')),
                  FooterLink('Disclaimer', () => Navigator.pushNamed(context, '/disclaimer')),
                ],
              ),
            ),
          ],
        ),
        const SizedBox(height: 24),
        const Text(
          'CONNECT WITH US',
          style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 10),
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            SocialChip('WhatsApp', Icons.chat_bubble, const Color(0xFF10B981), () {}),
            const SizedBox(width: 8),
            SocialChip('Facebook', Icons.flag, const Color(0xFF3B82F6), () {}),
            const SizedBox(width: 8),
            SocialChip('Email Us', Icons.email, const Color(0xFF8B5CF6), () {}),
          ],
        ),
        const SizedBox(height: 30),
        const Text(
          'JobsReport.online',
          style: TextStyle(fontWeight: FontWeight.black, color: Colors.white, fontSize: 14),
        ),
        const SizedBox(height: 4),
        const Text(
          'Real-time job market intelligence platform. Helping job seekers discover employment opportunities across Tanzania and beyond.',
          style: TextStyle(color: Color(0xFF64748B), fontSize: 11, height: 1.5),
        ),
        const SizedBox(height: 8),
        const Text(
          '© 2026 JobsReport',
          style: TextStyle(color: Color(0xFF475569), fontSize: 10, fontFamily: 'monospace'),
        ),
      ],
    );
  }

  String _getCountryFlag(String country) {
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
}

class StatBadge extends StatelessWidget {
  final IconData icon;
  final String count;
  final String label;
  final Color tint;

  const StatBadge({
    super.key,
    required this.icon,
    required this.count,
    required this.label,
    required this.tint,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      color: const Color(0xFF1E293B),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 12.0, horizontal: 8.0),
        child: Column(
          children: [
            Icon(icon, color: tint, size: 20),
            const SizedBox(height: 4),
            Text(
              count,
              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.black, fontSize: 18),
            ),
            Text(
              label,
              style: const TextStyle(color: Color(0xFF64748B), fontSize: 10),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ),
      ),
    );
  }
}

class FooterLink extends StatelessWidget {
  final String text;
  final VoidCallback onTap;

  const FooterLink(this.text, this.onTap, {super.key});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 4.0),
        child: Text(
          text,
          style: const TextStyle(color: Color(0xFF64748B), fontSize: 12),
        ),
      ),
    );
  }
}

class SocialChip extends StatelessWidget {
  final String label;
  final IconData icon;
  final Color tint;
  final VoidCallback onTap;

  const SocialChip(this.label, this.icon, this.tint, this.onTap, {super.key});

  @override
  Widget build(BuildContext context) {
    return Card(
      color: const Color(0xFF1E293B),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8),
        side: BorderSide(color: const Color(0xFF334155).withOpacity(0.3)),
      ),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 10.0, vertical: 6.0),
          child: Row(
            children: [
              Icon(icon, color: tint, size: 14),
              const SizedBox(width: 6),
              Text(
                label,
                style: const TextStyle(color: Color(0xFFE2E8F0), fontSize: 10, fontWeight: FontWeight.bold),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class CategoryMeta {
  final String name;
  final String slug;
  final IconData icon;
  final Color tint;
  final int jobCount;

  CategoryMeta(this.name, this.slug, this.icon, this.tint, {this.jobCount = 0});
}

class CountryInfo {
  final String name;
  final String flag;
  final Color tint;

  CountryInfo(this.name, this.flag, this.tint);
}
