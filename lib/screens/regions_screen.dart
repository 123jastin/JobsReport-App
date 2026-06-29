import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/jobs_provider.dart';
import '../models/models.dart';

class RegionItem {
  final String name;
  final String flag;
  final int activeJobsCount;
  final int totalJobsCount;
  final String description;
  final String primaryDomain;
  final String status;
  final String country;
  final String countrySlug;
  final String slug;

  RegionItem({
    required this.name,
    required this.flag,
    required this.activeJobsCount,
    required this.totalJobsCount,
    required this.description,
    required this.primaryDomain,
    required this.status,
    required this.country,
    required this.countrySlug,
    required this.slug,
  });
}

class RegionsScreen extends StatefulWidget {
  const RegionsScreen({super.key});

  @override
  State<RegionsScreen> createState() => _RegionsScreenState();
}

class _RegionsScreenState extends State<RegionsScreen> {
  final TextEditingController _searchController = TextEditingController();
  String _searchTerm = '';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<JobsProvider>(context, listen: false).fetchLocations();
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
    final allJobs = provider.allJobs;
    final liveLocations = provider.liveLocations;
    final isLocationsLoading = provider.isLocationsLoading;
    final selectedCountry = provider.selectedCountry;

    // Dynamically calculate counts based on active database state
    final List<RegionItem> regions = [];

    if (liveLocations.isNotEmpty) {
      final Map<String, RemoteLocation> uniqueRegions = {};
      for (final loc in liveLocations) {
        final key = loc.name.toLowerCase().trim();
        if (!uniqueRegions.containsKey(key)) {
          uniqueRegions[key] = loc;
        }
      }

      for (final loc in uniqueRegions.values) {
        final regionName = loc.name.toLowerCase().trim();
        final matchingJobs = allJobs.where((job) {
          final jobLoc = job.location.toLowerCase();
          return jobLoc.contains(regionName);
        }).toList();

        final activeJobsCount = matchingJobs.where((j) => j.active).length;
        final totalJobsCount = matchingJobs.length;

        final String flag;
        switch (loc.country.toLowerCase().trim()) {
          case 'tanzania':
            flag = '🇹🇿';
            break;
          case 'kenya':
            flag = '🇰🇪';
            break;
          case 'united states':
          case 'usa':
          case 'us':
            flag = '🇺🇸';
            break;
          case 'united kingdom':
          case 'uk':
            flag = '🇬🇧';
            break;
          case 'germany':
            flag = '🇩🇪';
            break;
          case 'south africa':
            flag = '🇿🇦';
            break;
          default:
            flag = '🌍';
        }

        final cleanCountry = loc.country.trim();
        final cleanRegionName = loc.name.trim();
        final countrySlug = cleanCountry.toLowerCase().replaceAll(RegExp(r'\s+'), '-');
        final slug = cleanRegionName.toLowerCase().replaceAll(RegExp(r'\s+'), '-');
        final primaryDomain = 'jobsreport.online/${cleanCountry.toLowerCase().replaceAll(RegExp(r'\s+'), '')}';

        final String description;
        switch (cleanCountry.toLowerCase()) {
          case 'tanzania':
            description = 'East Africa Digital Hub & fintech innovation surge center.';
            break;
          case 'united states':
            description = 'High-growth Silicon Valley, Austin, & Seattle hubs.';
            break;
          case 'united kingdom':
            description = 'London tech corridor & remote contract clusters.';
            break;
          case 'germany':
            description = 'Berlin fintech ecosystems and industrial software.';
            break;
          case 'kenya':
            description = 'Nairobi tech ecosystem & mobile banking surge.';
            break;
          case 'south africa':
            description = 'Cape Town creative clusters & digital telemetry.';
            break;
          default:
            description = 'International tech talent & regional employment hubs in $cleanCountry.';
        }

        regions.add(RegionItem(
          name: cleanRegionName,
          flag: flag,
          activeJobsCount: activeJobsCount,
          totalJobsCount: totalJobsCount,
          description: description,
          primaryDomain: primaryDomain,
          status: activeJobsCount > 0 ? 'ACTIVE SYNC' : 'STANDBY',
          country: cleanCountry,
          countrySlug: countrySlug,
          slug: slug,
        ));
      }
    } else {
      // Fallback regions
      final tzCount = allJobs.where((j) => j.active && (j.location.toLowerCase().contains('tanzania') || j.location.toLowerCase().contains('dar es salaam'))).length;
      final usCount = allJobs.where((j) => j.active && (j.location.toLowerCase().contains('united states') || j.location.contains('CA') || j.location.contains('NY') || j.location.contains('TX') || j.location.contains('WA') || j.location.contains('IL') || j.location.toLowerCase().contains('chicago') || j.location.toLowerCase().contains('san francisco') || j.location.toLowerCase().contains('austin') || j.location.toLowerCase().contains('seattle'))).length;
      final ukCount = allJobs.where((j) => j.active && (j.location.toLowerCase().contains('united kingdom') || j.location.toLowerCase().contains('london') || j.location.toLowerCase().contains('manchester') || j.location.toLowerCase().contains('uk'))).length;
      final deCount = allJobs.where((j) => j.active && (j.location.toLowerCase().contains('germany') || j.location.toLowerCase().contains('berlin') || j.location.toLowerCase().contains('munich') || j.location.toLowerCase().contains('de'))).length;
      final keCount = allJobs.where((j) => j.active && (j.location.toLowerCase().contains('kenya') || j.location.toLowerCase().contains('nairobi'))).length;
      final zaCount = allJobs.where((j) => j.active && (j.location.toLowerCase().contains('south africa') || j.location.toLowerCase().contains('cape town'))).length;

      final tzTotal = allJobs.where((j) => j.location.toLowerCase().contains('tanzania') || j.location.toLowerCase().contains('dar es salaam')).length;
      final usTotal = allJobs.where((j) => j.location.toLowerCase().contains('united states') || j.location.contains('CA') || j.location.contains('NY') || j.location.contains('TX') || j.location.contains('WA') || j.location.contains('IL') || j.location.toLowerCase().contains('chicago') || j.location.toLowerCase().contains('san francisco') || j.location.toLowerCase().contains('austin') || j.location.toLowerCase().contains('seattle')).length;
      final ukTotal = allJobs.where((j) => j.location.toLowerCase().contains('united kingdom') || j.location.toLowerCase().contains('london') || j.location.toLowerCase().contains('manchester') || j.location.toLowerCase().contains('uk')).length;
      final deTotal = allJobs.where((j) => j.location.toLowerCase().contains('germany') || j.location.toLowerCase().contains('berlin') || j.location.toLowerCase().contains('munich') || j.location.toLowerCase().contains('de')).length;
      final keTotal = allJobs.where((j) => j.location.toLowerCase().contains('kenya') || j.location.toLowerCase().contains('nairobi')).length;
      final zaTotal = allJobs.where((j) => j.location.toLowerCase().contains('south africa') || j.location.toLowerCase().contains('cape town')).length;

      regions.addAll([
        RegionItem(name: 'Tanzania', flag: '🇹🇿', activeJobsCount: tzCount, totalJobsCount: tzTotal, description: 'East Africa Digital Hub & fintech innovation surge center.', primaryDomain: 'jobsreport.online/tz', status: tzCount > 0 ? 'ACTIVE SYNC' : 'STANDBY', country: 'Tanzania', countrySlug: 'tanzania', slug: 'tanzania'),
        RegionItem(name: 'United States', flag: '🇺🇸', activeJobsCount: usCount, totalJobsCount: usTotal, description: 'High-growth Silicon Valley, Austin, & Seattle hubs.', primaryDomain: 'jobsreport.online/us', status: usCount > 0 ? 'ACTIVE SYNC' : 'STANDBY', country: 'United States', countrySlug: 'united-states', slug: 'united-states'),
        RegionItem(name: 'United Kingdom', flag: '🇬🇧', activeJobsCount: ukCount, totalJobsCount: ukTotal, description: 'London tech corridor & remote contract clusters.', primaryDomain: 'jobsreport.online/uk', status: ukCount > 0 ? 'ACTIVE SYNC' : 'STANDBY', country: 'United Kingdom', countrySlug: 'united-kingdom', slug: 'united-kingdom'),
        RegionItem(name: 'Germany', flag: '🇩🇪', activeJobsCount: deCount, totalJobsCount: deTotal, description: 'Berlin fintech ecosystems and industrial software.', primaryDomain: 'jobsreport.online/de', status: deCount > 0 ? 'ACTIVE SYNC' : 'STANDBY', country: 'Germany', countrySlug: 'germany', slug: 'germany'),
        RegionItem(name: 'Kenya', flag: '🇰🇪', activeJobsCount: keCount, totalJobsCount: keTotal, description: 'Nairobi tech ecosystem & mobile banking surge.', primaryDomain: 'jobsreport.online/ke', status: keCount > 0 ? 'ACTIVE SYNC' : 'STANDBY', country: 'Kenya', countrySlug: 'kenya', slug: 'kenya'),
        RegionItem(name: 'South Africa', flag: '🇿🇦', activeJobsCount: zaCount, totalJobsCount: zaTotal, description: 'Cape Town creative clusters & digital telemetry.', primaryDomain: 'jobsreport.online/za', status: zaCount > 0 ? 'ACTIVE SYNC' : 'STANDBY', country: 'South Africa', countrySlug: 'south-africa', slug: 'south-africa'),
      ]);
    }

    // Filter by selected country
    final countryFilteredRegions = selectedCountry == 'Worldwide'
        ? regions
        : regions.where((r) => r.country.toLowerCase() == selectedCountry.toLowerCase()).toList();

    // Filter by search term
    final filteredRegions = _searchTerm.isEmpty
        ? countryFilteredRegions
        : countryFilteredRegions.where((r) {
            return r.name.toLowerCase().contains(_searchTerm.toLowerCase()) ||
                r.country.toLowerCase().contains(_searchTerm.toLowerCase());
          }).toList();

    // Separate active vs standby
    final regionsWithJobs = filteredRegions.where((r) => r.totalJobsCount > 0).toList();
    final regionsWithoutJobs = filteredRegions.where((r) => r.totalJobsCount == 0).toList();

    // Compute stats
    final totalActiveJobs = regionsWithJobs.fold<int>(0, (sum, item) => sum + item.activeJobsCount);
    final locationsWithJobs = regionsWithJobs.length;
    final totalLocationsCount = filteredRegions.length;

    // Group active ones by country
    final Map<String, List<RegionItem>> groupedByCountry = {};
    for (final r in regionsWithJobs) {
      groupedByCountry.putIfAbsent(r.country, () => []).add(r);
    }

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: const Text('Regional Job Explorer', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(16.0),
          children: [
            // Title Header Section
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: const [
                    Icon(Icons.place, color: Color(0xFFF59E0B), size: 16),
                    SizedBox(width: 6),
                    Text(
                      'REGIONAL JOB EXPLORER',
                      style: TextStyle(fontFamily: 'monospace', fontSize: 11, fontWeight: FontWeight.bold, color: Color(0xFFF59E0B), letterSpacing: 1.0),
                    ),
                  ],
                ),
                if (isLocationsLoading)
                  Row(
                    children: const [
                      SizedBox(
                        width: 10,
                        height: 10,
                        child: CircularProgressIndicator(color: Color(0xFFF59E0B), strokeWidth: 1.2),
                      ),
                      SizedBox(width: 4),
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
              selectedCountry == 'Worldwide' ? 'Jobs by City & Region' : 'Jobs by Region in $selectedCountry',
              style: const TextStyle(color: Colors.white, fontSize: 26, fontWeight: FontWeight.black, letterSpacing: -0.5),
            ),
            const SizedBox(height: 8),

            Text(
              selectedCountry == 'Worldwide'
                  ? 'Browse job opportunities across $locationsWithJobs cities and regions worldwide. $totalActiveJobs active jobs available.'
                  : 'Browse job opportunities across $locationsWithJobs regions in $selectedCountry. $totalActiveJobs active jobs available.',
              style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 14, height: 1.4),
            ),
            const SizedBox(height: 16),

            // Country Active Filter Banner
            if (selectedCountry != 'Worldwide') ...[
              Card(
                color: const Color(0xFF1E293B).withOpacity(0.6),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                  side: const BorderSide(color: Color(0xFF3B82F6), width: 0.5),
                ),
                child: InkWell(
                  onTap: () {
                    provider.setSelectedCountry('Worldwide');
                  },
                  borderRadius: BorderRadius.circular(12),
                  child: Padding(
                    padding: const EdgeInsets.all(12.0),
                    child: Row(
                      children: [
                        const Icon(Icons.filter_list, color: Color(0xFF60A5FA), size: 16),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            'Filter: $selectedCountry active. Tap to reset to Worldwide.',
                            style: const TextStyle(color: Color(0xFF60A5FA), fontSize: 11, fontWeight: FontWeight.medium),
                          ),
                        ),
                        const Icon(Icons.close, color: Color(0xFF64748B), size: 16),
                      ],
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 16),
            ],

            // Stats Cards Row
            Row(
              children: [
                Expanded(
                  child: _buildStatCard(Icons.place, 'Regions', '$locationsWithJobs', const Color(0xFFF59E0B)),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: _buildStatCard(Icons.work, 'Active Jobs', '$totalActiveJobs', const Color(0xFF3B82F6)),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: _buildStatCard(Icons.public, 'Locations', '$totalLocationsCount', const Color(0xFF94A3B8)),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Ad Banner
            _buildRegionAdBannerCard('4550717155'),
            const SizedBox(height: 16),

            // Search Bar
            TextField(
              controller: _searchController,
              onChanged: (val) {
                setState(() {
                  _searchTerm = val;
                });
              },
              style: const TextStyle(color: Colors.white, fontSize: 14),
              decoration: InputDecoration(
                hintText: selectedCountry == 'Worldwide' ? 'Search cities or countries...' : 'Search regions in $selectedCountry...',
                hintStyle: const TextStyle(color: Color(0xFF64748B), fontSize: 13),
                prefixIcon: const Icon(Icons.search, color: Color(0xFF475569)),
                suffixIcon: _searchTerm.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.close, color: Color(0xFF64748B)),
                        onPressed: () {
                          _searchController.clear();
                          setState(() {
                            _searchTerm = '';
                          });
                        },
                      )
                    : null,
                filled: true,
                fillColor: const Color(0xFF1E293B).withOpacity(0.3),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(16),
                  borderSide: const BorderSide(color: Color(0xFFF59E0B), width: 1.5),
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(16),
                  borderSide: const BorderSide(color: Color(0xFF334155)),
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Empty State
            if (filteredRegions.isEmpty) ...[
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 48.0),
                child: Column(
                  children: [
                    const Icon(Icons.place, size: 54, color: Color(0xFF475569)),
                    const SizedBox(height: 12),
                    const Text('No Regions Found', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 18)),
                    const SizedBox(height: 6),
                    const Text('No locations matched your search criteria.', style: TextStyle(color: Color(0xFF64748B), fontSize: 13)),
                  ],
                ),
              ),
            ],

            // Active Regions Grouped by Country / Flat List
            if (selectedCountry == 'Worldwide' && regionsWithJobs.isNotEmpty) ...[
              ...groupedByCountry.entries.map((entry) {
                final country = entry.key;
                final list = entry.value;
                final firstFlag = list.first.flag;
                final firstSlug = list.first.countrySlug;

                return Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Padding(
                      padding: const EdgeInsets.symmetric(vertical: 8.0),
                      child: Row(
                        children: [
                          Text(firstFlag, style: const TextStyle(fontSize: 18)),
                          const SizedBox(width: 8),
                          Text(
                            country.toUpperCase(),
                            style: const TextStyle(color: Colors.white, fontFamily: 'monospace', fontWeight: FontWeight.bold, fontSize: 13, letterSpacing: 1.0),
                          ),
                          const SizedBox(width: 8),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                            decoration: BoxDecoration(color: const Color(0xFF3B82F6).withOpacity(0.15), borderRadius: BorderRadius.circular(100)),
                            child: Text(
                              '${list.length} REGION${list.length > 1 ? "S" : ""}',
                              style: const TextStyle(color: Color(0xFF60A5FA), fontSize: 8, fontWeight: FontWeight.bold),
                            ),
                          ),
                          const Spacer(),
                          InkWell(
                            onTap: () {
                              Navigator.pushNamed(context, 'country/$firstSlug');
                            },
                            child: const Text('View Country →', style: TextStyle(color: Color(0xFF3B82F6), fontWeight: FontWeight.bold, fontSize: 10)),
                          ),
                        ],
                      ),
                    ),
                    ...List.generate(list.length, (idx) {
                      final item = list[idx];
                      return Column(
                        children: [
                          _buildRegionRowCard(item, provider),
                          if ((idx + 1) % 3 == 0 && idx < list.length - 1) ...[
                            const SizedBox(height: 12),
                            _buildInFeedAdCard('slot_${country}_$idx'),
                            const SizedBox(height: 12),
                          ],
                        ],
                      );
                    }),
                  ],
                );
              }),
            ] else if (regionsWithJobs.isNotEmpty) ...[
              ...List.generate(regionsWithJobs.length, (idx) {
                final item = regionsWithJobs[idx];
                return Column(
                  children: [
                    _buildRegionRowCard(item, provider),
                    if ((idx + 1) % 3 == 0 && idx < regionsWithJobs.length - 1) ...[
                      const SizedBox(height: 12),
                      _buildInFeedAdCard('slot_flat_$idx'),
                      const SizedBox(height: 12),
                    ],
                  ],
                );
              }),
            ],

            // Standby chips
            if (regionsWithoutJobs.isNotEmpty) ...[
              const SizedBox(height: 20),
              Row(
                children: [
                  const Icon(Icons.place, color: Color(0xFF64748B), size: 14),
                  const SizedBox(width: 8),
                  Text(
                    'OTHER LOCATIONS (${regionsWithoutJobs.length})',
                    style: const TextStyle(color: Color(0xFF64748B), fontWeight: FontWeight.bold, fontFamily: 'monospace', fontSize: 11, letterSpacing: 1.0),
                  ),
                  const SizedBox(width: 8),
                  const Text('Standby Network • No active jobs', style: TextStyle(color: Color(0xFF475569), fontSize: 9)),
                ],
              ),
              const SizedBox(height: 12),
              _buildStandbyGrid(regionsWithoutJobs, provider),
            ],

            const SizedBox(height: 24),
            _buildRegionAdBannerCard('5466053430'),
          ],
        ),
      ),
    );
  }

  Widget _buildStatCard(IconData icon, String label, String value, Color color) {
    return Card(
      color: const Color(0xFF1E293B),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(icon, color: color, size: 16),
                const SizedBox(width: 6),
                Expanded(
                  child: Text(
                    label,
                    style: const TextStyle(color: Color(0xFF64748B), fontSize: 10, fontWeight: FontWeight.bold),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 4),
            Text(value, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.black, fontSize: 20)),
          ],
        ),
      ),
    );
  }

  Widget _buildRegionRowCard(RegionItem region, JobsProvider provider) {
    final isSyncActive = region.activeJobsCount > 0;

    return Padding(
      padding: const EdgeInsets.only(bottom: 12.0),
      child: Card(
        color: const Color(0xFF1E293B).withOpacity(0.4),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: BorderSide(color: isSyncActive ? const Color(0xFF3B82F6).withOpacity(0.3) : const Color(0xFF334155).withOpacity(0.3)),
        ),
        child: InkWell(
          onTap: () {
            provider.setSelectedCountry(region.country);
            Navigator.pushNamed(context, 'region_detail/${Uri.encodeComponent(region.name)}');
          },
          borderRadius: BorderRadius.circular(16),
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Row(
              children: [
                Container(
                  width: 44,
                  height: 44,
                  decoration: BoxDecoration(
                    color: Colors.white.withOpacity(0.05),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: Colors.white.withOpacity(0.1)),
                  ),
                  alignment: Alignment.center,
                  child: Text(region.flag, style: const TextStyle(fontSize: 22)),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Expanded(
                            child: Text(
                              region.name,
                              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 15),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                          const SizedBox(width: 6),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                            decoration: BoxDecoration(
                              color: isSyncActive ? const Color(0xFF10B981).withOpacity(0.15) : const Color(0xFF64748B).withOpacity(0.15),
                              borderRadius: BorderRadius.circular(4),
                            ),
                            child: Text(
                              region.status,
                              style: TextStyle(color: isSyncActive ? const Color(0xFF34D399) : const Color(0xFF94A3B8), fontSize: 8, fontWeight: FontWeight.bold),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text(
                        region.description,
                        style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 11),
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                      const SizedBox(height: 6),
                      Row(
                        children: [
                          Text('🌐 ${region.primaryDomain}', style: const TextStyle(color: Color(0xFF60A5FA), fontSize: 11, fontWeight: FontWeight.bold)),
                          const SizedBox(width: 12),
                          const Text('•', style: TextStyle(color: Color(0xFF475569), fontSize: 11)),
                          const SizedBox(width: 12),
                          Icon(Icons.work, size: 11, color: isSyncActive ? const Color(0xFF34D399) : const Color(0xFF64748B)),
                          const SizedBox(width: 4),
                          Text(
                            '${region.activeJobsCount} Active',
                            style: TextStyle(color: isSyncActive ? const Color(0xFF34D399) : const Color(0xFF64748B), fontWeight: FontWeight.bold, fontSize: 11),
                          ),
                          if (region.totalJobsCount > region.activeJobsCount) ...[
                            const SizedBox(width: 4),
                            Text('(${region.totalJobsCount} total)', style: const TextStyle(color: Color(0xFF475569), fontSize: 10)),
                          ],
                        ],
                      ),
                    ],
                  ),
                ),
                const Icon(Icons.chevron_right, color: Color(0xFF475569), size: 20),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildStandbyGrid(List<RegionItem> list, JobsProvider provider) {
    // Render chips in a chunked Grid of 2 items wide
    final List<Widget> rows = [];
    final chunked = <List<RegionItem>>[];
    for (var i = 0; i < list.length; i += 2) {
      chunked.add(list.subList(i, i + 2 > list.length ? list.length : i + 2));
    }

    for (final chunk in chunked) {
      rows.add(
        Padding(
          padding: const EdgeInsets.only(bottom: 8.0),
          child: Row(
            children: [
              Expanded(
                child: _buildStandbyChip(chunk[0], provider),
              ),
              const SizedBox(width: 8),
              if (chunk.length > 1)
                Expanded(
                  child: _buildStandbyChip(chunk[1], provider),
                )
              else
                const Expanded(child: SizedBox()),
            ],
          ),
        ),
      );
    }

    return Column(children: rows);
  }

  Widget _buildStandbyChip(RegionItem region, JobsProvider provider) {
    return Card(
      color: const Color(0xFF1E293B).withOpacity(0.3),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10),
        side: BorderSide(color: Colors.white.withOpacity(0.05)),
      ),
      child: InkWell(
        onTap: () {
          provider.setSelectedCountry(region.country);
          Navigator.pushNamed(context, 'region_detail/${Uri.encodeComponent(region.name)}');
        },
        borderRadius: BorderRadius.circular(10),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),
          child: Row(
            children: [
              Text(region.flag, style: const TextStyle(fontSize: 14)),
              const SizedBox(width: 6),
              Expanded(
                child: Text(
                  region.name,
                  style: const TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.bold),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
              const Icon(Icons.chevron_right, color: Color(0xFF64748B), size: 12),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildRegionAdBannerCard(String slotId) {
    return Card(
      color: Colors.white.withOpacity(0.01),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: Colors.white.withOpacity(0.05)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          children: [
            const Text(
              'JOBSREPORT VERIFIED ADVERTISEMENT',
              style: TextStyle(fontFamily: 'monospace', fontSize: 9, fontWeight: FontWeight.bold, color: Color(0xFF475569), letterSpacing: 1.0),
            ),
            const SizedBox(height: 2),
            Text(
              'Secure Bridge Slot $slotId Active',
              style: const TextStyle(fontFamily: 'monospace', fontSize: 8, color: Color(0xFF334155)),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildInFeedAdCard(String slotId) {
    return Card(
      color: Colors.white.withOpacity(0.02),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(color: Colors.white.withOpacity(0.05)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'TELEMETRY IN-FEED SPONSOR',
                  style: TextStyle(fontFamily: 'monospace', fontSize: 8, fontWeight: FontWeight.bold, color: Color(0xFF3B82F6), letterSpacing: 1.0),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                  decoration: BoxDecoration(color: const Color(0xFF10B981).withOpacity(0.1), borderRadius: BorderRadius.circular(4)),
                  child: const Text('SAFE BRIDGE', style: TextStyle(color: Color(0xFF10B981), fontSize: 8, fontWeight: FontWeight.bold)),
                ),
              ],
            ),
            const SizedBox(height: 8),
            const Text('Verified Partner Ad', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 13)),
            const SizedBox(height: 4),
            const Text(
              'Ads delivered via JobsReport Telemetry slot are end-to-end sandbox verified and scanned for secure redirection routing.',
              style: TextStyle(color: Color(0xFF64748B), fontSize: 11, height: 1.4),
            ),
          ],
        ),
      ),
    );
  }
}
