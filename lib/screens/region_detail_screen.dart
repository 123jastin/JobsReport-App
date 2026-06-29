import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../providers/jobs_provider.dart';
import '../models/models.dart';

class RegionDetailScreen extends StatelessWidget {
  final String regionName;

  const RegionDetailScreen({
    super.key,
    required this.regionName,
  });

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<JobsProvider>(context);
    final allJobs = provider.allJobs;
    final liveLocations = provider.liveLocations;
    final isLocationsLoading = provider.isLocationsLoading;
    final isMarketLoading = provider.isMarketLoading;

    // Find matched location info
    final String regionNameLower = regionName.toLowerCase().trim();
    final String regionSlugLower = regionNameLower.replaceAll(RegExp(r'\s+'), '-');

    RemoteLocation matchedLocation = liveLocations.firstWhere(
      (loc) {
        final locNameLower = loc.name.toLowerCase().trim();
        return locNameLower == regionNameLower ||
            locNameLower.replaceAll(RegExp(r'\s+'), '-') == regionSlugLower;
      },
      orElse: () => RemoteLocation(
        name: regionName,
        country: _getCountryForRegion(regionNameLower),
        region: _getRegionLabel(regionNameLower),
        postcode: _getPostcode(regionNameLower),
      ),
    );

    // Filter jobs for this region
    final matchedRegionLower = matchedLocation.region?.toLowerCase().trim();
    final filteredJobs = allJobs.where((job) {
      final loc = job.location.toLowerCase().trim();
      final locSlug = loc.replaceAll(RegExp(r'\s+'), '-');

      final matchesName = loc.contains(regionNameLower);
      final matchesRegion = matchedRegionLower != null && loc.contains(matchedRegionLower);
      final matchesSlug = locSlug.contains(regionSlugLower);

      final matchesUS = regionNameLower == 'united states' &&
          (loc.contains('ca') ||
              loc.contains('ny') ||
              loc.contains('tx') ||
              loc.contains('wa') ||
              loc.contains('il') ||
              loc.contains('chicago') ||
              loc.contains('san francisco') ||
              loc.contains('austin') ||
              loc.contains('seattle'));

      final matchesUK = regionNameLower == 'united kingdom' &&
          (loc.contains('london') || loc.contains('manchester') || loc.contains('uk'));

      final matchesDE = regionNameLower == 'germany' &&
          (loc.contains('berlin') || loc.contains('munich') || loc.contains('de'));

      return matchesName || matchesRegion || matchesSlug || matchesUS || matchesUK || matchesDE;
    }).toList();

    // Sort by active jobs first (Active before Expired), then by datePosted descending
    filteredJobs.sort((a, b) {
      if (a.active && !b.active) return -1;
      if (!a.active && b.active) return 1;
      return b.datePosted.compareTo(a.datePosted);
    });

    final activeJobs = filteredJobs.where((j) => j.active).toList();
    final expiredJobs = filteredJobs.where((j) => !j.active).toList();
    final companiesCount = filteredJobs.map((j) => j.company).toSet().length;

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: Text(regionName, style: const TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(16.0),
          children: [
            // 1. Breadcrumbs
            Row(
              children: [
                InkWell(
                  onTap: () {
                    Navigator.pushReplacementNamed(context, '/home');
                  },
                  child: const Text(
                    'HOME',
                    style: TextStyle(fontFamily: 'monospace', fontSize: 10, color: Color(0xFF64748B)),
                  ),
                ),
                const SizedBox(width: 6),
                const Text('/', style: TextStyle(fontSize: 10, color: Color(0xFF475569))),
                const SizedBox(width: 6),
                InkWell(
                  onTap: () {
                    Navigator.pop(context);
                  },
                  child: const Text(
                    'REGIONS',
                    style: TextStyle(fontFamily: 'monospace', fontSize: 10, color: Color(0xFF64748B)),
                  ),
                ),
                const SizedBox(width: 6),
                const Text('/', style: TextStyle(fontSize: 10, color: Color(0xFF475569))),
                const SizedBox(width: 6),
                Text(
                  regionName.toUpperCase(),
                  style: const TextStyle(fontFamily: 'monospace', fontSize: 10, color: Color(0xFFFBBF24), fontWeight: FontWeight.bold),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // 2. Header Title Section
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: const [
                    Icon(Icons.place, color: Color(0xFFF59E0B), size: 16),
                    SizedBox(width: 6),
                    Text(
                      'REGIONAL JOB MARKET',
                      style: TextStyle(fontFamily: 'monospace', fontSize: 11, fontWeight: FontWeight.bold, color: Color(0xFFF59E0B), letterSpacing: 1.0),
                    ),
                  ],
                ),
                if (isLocationsLoading || isMarketLoading)
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
              'Jobs in $regionName',
              style: const TextStyle(color: Colors.white, fontSize: 26, fontWeight: FontWeight.black, letterSpacing: -0.5),
            ),
            const SizedBox(height: 8),

            Text(
              'Browse ${activeJobs.length} active job opportunities in $regionName${matchedLocation.postcode != null && matchedLocation.postcode!.isNotEmpty ? " • Postcode: ${matchedLocation.postcode}" : ""}.',
              style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 14, height: 1.4),
            ),
            const SizedBox(height: 16),

            // 3. Stats Section
            Row(
              children: [
                Expanded(
                  child: _buildSimpleStatCard(Icons.work, '${activeJobs.length}', 'Active Jobs', const Color(0xFFF59E0B)),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: _buildSimpleStatCard(Icons.business, '$companiesCount', 'Companies', const Color(0xFF3B82F6)),
                ),
                if (expiredJobs.isNotEmpty) ...[
                  const SizedBox(width: 10),
                  Expanded(
                    child: _buildSimpleStatCard(Icons.warning, '${expiredJobs.length}', 'Expired', const Color(0xFFEF4444)),
                  ),
                ],
              ],
            ),
            const SizedBox(height: 16),

            // Ad banner
            _buildRegionAdBannerCard('4550717155'),
            const SizedBox(height: 16),

            // 5. Jobs List
            if (filteredJobs.isEmpty) ...[
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 48.0),
                child: Column(
                  children: [
                    const Icon(Icons.place, size: 54, color: Color(0xFF475569)),
                    const SizedBox(height: 12),
                    Text('No Jobs in $regionName', style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 18)),
                    const SizedBox(height: 6),
                    const Text('No job listings are currently available for this region.', style: TextStyle(color: Color(0xFF64748B), fontSize: 13)),
                    const SizedBox(height: 16),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        ElevatedButton(
                          onPressed: () => Navigator.pop(context),
                          style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF3B82F6)),
                          child: const Text('← Browse Regions', style: TextStyle(color: Colors.white)),
                        ),
                        const SizedBox(width: 12),
                        TextButton(
                          onPressed: () {
                            Navigator.pushReplacementNamed(context, '/market');
                          },
                          child: const Text('View All Jobs →', style: TextStyle(color: Colors.white)),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ] else ...[
              // Active Jobs Section
              if (activeJobs.isNotEmpty) ...[
                Text(
                  'ACTIVE JOBS IN ${regionName.toUpperCase()} (${activeJobs.length})',
                  style: const TextStyle(fontFamily: 'monospace', fontSize: 12, fontWeight: FontWeight.bold, color: Color(0xFFFBBF24)),
                ),
                const SizedBox(height: 12),
                ...List.generate(activeJobs.length, (idx) {
                  final job = activeJobs[idx];
                  return Column(
                    children: [
                      _buildJobRowItem(context, job, false),
                      if ((idx + 1) % 3 == 0 && idx < activeJobs.length - 1) ...[
                        const SizedBox(height: 12),
                        _buildInFeedAdCard('slot_active_$idx'),
                        const SizedBox(height: 12),
                      ],
                    ],
                  );
                }),
              ],

              // Expired Jobs Section
              if (expiredJobs.isNotEmpty) ...[
                const SizedBox(height: 20),
                Text(
                  'EXPIRED LISTINGS (${expiredJobs.length})',
                  style: const TextStyle(fontFamily: 'monospace', fontSize: 12, fontWeight: FontWeight.bold, color: Color(0xFF64748B)),
                ),
                const SizedBox(height: 12),
                ...expiredJobs.map((job) => _buildJobRowItem(context, job, true)).toList(),
              ],
            ],

            const SizedBox(height: 24),
            _buildRegionAdBannerCard('5466053430'),
          ],
        ),
      ),
    );
  }

  String _getCountryForRegion(String regionLower) {
    switch (regionLower) {
      case 'tanzania':
      case 'dar es salaam':
        return 'Tanzania';
      case 'united states':
      case 'california':
        return 'United States';
      case 'united kingdom':
      case 'greater london':
      case 'london':
        return 'United Kingdom';
      case 'germany':
      case 'berlin':
      case 'berlin state':
        return 'Germany';
      case 'kenya':
      case 'nairobi':
      case 'nairobi county':
        return 'Kenya';
      case 'south africa':
      case 'cape town':
      case 'western cape':
        return 'South Africa';
      default:
        return 'Worldwide';
    }
  }

  String? _getRegionLabel(String regionLower) {
    switch (regionLower) {
      case 'tanzania':
      case 'dar es salaam':
        return 'Dar es Salaam';
      case 'united states':
      case 'california':
        return 'California';
      case 'united kingdom':
      case 'greater london':
      case 'london':
        return 'Greater London';
      case 'germany':
      case 'berlin':
      case 'berlin state':
        return 'Berlin State';
      case 'kenya':
      case 'nairobi':
      case 'nairobi county':
        return 'Nairobi County';
      case 'south africa':
      case 'cape town':
      case 'western cape':
        return 'Western Cape';
      default:
        return null;
    }
  }

  String? _getPostcode(String regionLower) {
    switch (regionLower) {
      case 'tanzania':
      case 'dar es salaam':
        return '11101';
      case 'united states':
      case 'california':
        return '94025';
      case 'united kingdom':
      case 'greater london':
      case 'london':
        return 'EC1A 1BB';
      case 'germany':
      case 'berlin':
      case 'berlin state':
        return '10115';
      case 'kenya':
      case 'nairobi':
      case 'nairobi county':
        return '00100';
      case 'south africa':
      case 'cape town':
      case 'western cape':
        return '8001';
      default:
        return null;
    }
  }

  Widget _buildSimpleStatCard(IconData icon, String value, String label, Color tintColor) {
    return Card(
      color: const Color(0xFF1E293B),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Row(
          children: [
            Icon(icon, color: tintColor, size: 20),
            const SizedBox(width: 8),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(value, style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.white, fontSize: 16)),
                  Text(label, style: const TextStyle(color: Color(0xFF64748B), fontSize: 10), maxLines: 1, overflow: TextOverflow.ellipsis),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildJobRowItem(BuildContext context, JobEntity job, bool isExpired) {
    final isUrl = job.logoResName.startsWith('http://') || job.logoResName.startsWith('https://');

    return Opacity(
      opacity: isExpired ? 0.5 : 1.0,
      child: Padding(
        padding: const EdgeInsets.only(bottom: 12.0),
        child: Card(
          color: const Color(0xFF1E293B).withOpacity(0.4),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
            side: BorderSide(color: isExpired ? const Color(0xFF334155).withOpacity(0.2) : const Color(0xFF334155).withOpacity(0.5)),
          ),
          child: InkWell(
            onTap: () {
              if (isExpired) {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('This listing has expired.')),
                );
              } else {
                Navigator.pushNamed(context, 'job_detail/${job.id}');
              }
            },
            borderRadius: BorderRadius.circular(16),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Row(
                children: [
                  // Logo
                  Container(
                    width: 44,
                    height: 44,
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.05),
                      borderRadius: BorderRadius.circular(12),
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
                              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                            ),
                          )
                        : Text(
                            job.company.isNotEmpty ? job.company.substring(0, 1).toUpperCase() : '?',
                            style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
                          ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            if (isExpired)
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                decoration: BoxDecoration(color: const Color(0xFFEF4444).withOpacity(0.15), borderRadius: BorderRadius.circular(4)),
                                child: const Text('EXPIRED', style: TextStyle(color: Color(0xFFEF4444), fontSize: 8, fontWeight: FontWeight.bold)),
                              )
                            else
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                decoration: BoxDecoration(color: const Color(0xFF3B82F6).withOpacity(0.15), borderRadius: BorderRadius.circular(4)),
                                child: Text(job.type.toUpperCase(), style: const TextStyle(color: Color(0xFF60A5FA), fontSize: 8, fontWeight: FontWeight.bold)),
                              ),
                            const SizedBox(width: 6),
                            Text(
                              job.workplace,
                              style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 10, fontWeight: FontWeight.medium),
                            ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Text(
                          job.title,
                          style: TextStyle(color: isExpired ? const Color(0xFF64748B) : Colors.white, fontWeight: FontWeight.bold, fontSize: 15),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        const SizedBox(height: 4),
                        Row(
                          children: [
                            Text(job.company, style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12), maxLines: 1, overflow: TextOverflow.ellipsis),
                            const SizedBox(width: 10),
                            const Text('•', style: TextStyle(color: Color(0xFF475569), fontSize: 12)),
                            const SizedBox(width: 10),
                            Expanded(
                              child: Text(
                                job.location,
                                style: const TextStyle(color: Color(0xFF64748B), fontSize: 11),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ),
                          ],
                        ),
                        if (job.salary.isNotEmpty) ...[
                          const SizedBox(height: 4),
                          Text(
                            job.salary,
                            style: const TextStyle(fontFamily: 'monospace', color: Color(0xFF10B981), fontWeight: FontWeight.bold, fontSize: 11),
                          ),
                        ],
                      ],
                    ),
                  ),
                ],
              ),
            ),
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
        padding: const EdgeInsets.all(16.0),
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
