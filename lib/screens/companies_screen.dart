import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../providers/jobs_provider.dart';
import '../models/models.dart';

class Company {
  final String id;
  final String name;
  final String website;
  final String description;
  final String industry;
  final String? foundedYear;
  final String? employeeCount;
  final String? streetAddress;
  final String? area;
  final String? locality;
  final String? district;
  final String? postalCode;
  final String? country;
  final String? logoResName;
  final int totalJobs;
  final int activeJobs;
  final List<JobEntity> jobs;
  final String? logoUrl;

  Company({
    required this.id,
    required this.name,
    required this.website,
    required this.description,
    required this.industry,
    this.foundedYear,
    this.employeeCount,
    this.streetAddress,
    this.area,
    this.locality,
    this.district,
    this.postalCode,
    this.country,
    this.logoResName,
    required this.totalJobs,
    required this.activeJobs,
    required this.jobs,
    this.logoUrl,
  });
}

class CompaniesScreen extends StatefulWidget {
  const CompaniesScreen({super.key});

  @override
  State<CompaniesScreen> createState() => _CompaniesScreenState();
}

class _CompaniesScreenState extends State<CompaniesScreen> {
  Company? _selectedCompany;
  String _searchTerm = '';
  int _currentPage = 1;
  static const int companiesPerPage = 12;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final provider = Provider.of<JobsProvider>(context, listen: false);
      if (provider.liveCompanies.isEmpty) {
        provider.fetchLiveCompanies();
      }
    });
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
    final allJobs = provider.allJobs;
    final liveCompanies = provider.liveCompanies;
    final isCompaniesLoading = provider.isCompaniesLoading;

    // Build the dynamic company structures
    final List<Company> companies = [];
    if (liveCompanies.isNotEmpty) {
      for (final rc in liveCompanies) {
        final companyJobs = allJobs.where((j) => j.company.toLowerCase() == rc.name.toLowerCase()).toList();
        companyJobs.sort((a, b) => b.datePosted.compareTo(a.datePosted));
        companies.add(Company(
          id: rc.id ?? rc.name,
          name: rc.name,
          website: rc.website ?? 'https://jobsreport.online',
          description: rc.description ?? 'Verified partner of JobsReport platform.',
          industry: rc.industry ?? 'Technology',
          foundedYear: rc.foundedYear,
          employeeCount: rc.employeeCount,
          streetAddress: rc.streetAddress,
          area: rc.area,
          locality: rc.locality ?? 'Remote',
          district: rc.district,
          postalCode: rc.postalCode,
          country: rc.country ?? 'TZ',
          logoResName: rc.logoUrl ?? (companyJobs.isNotEmpty ? companyJobs.first.logoResName : ''),
          totalJobs: rc.totalJobs ?? companyJobs.length,
          activeJobs: rc.activeJobs ?? companyJobs.length,
          jobs: companyJobs,
          logoUrl: rc.logoUrl,
        ));
      }
    } else {
      // Group by company name if API is pending/failed
      final grouped = <String, List<JobEntity>>{};
      for (final j in allJobs) {
        grouped.putIfAbsent(j.company, () => []).add(j);
      }
      grouped.forEach((companyName, companyJobs) {
        companyJobs.sort((a, b) => b.datePosted.compareTo(a.datePosted));
        final firstJob = companyJobs.first;
        companies.add(Company(
          id: companyName,
          name: companyName,
          website: firstJob.companyWebsite.isNotEmpty ? firstJob.companyWebsite : 'https://jobsreport.online',
          description: 'Verified partner of JobsReport platform offering career advancement pathways.',
          industry: firstJob.category.isNotEmpty ? firstJob.category : 'General',
          locality: 'Remote',
          logoResName: firstJob.logoResName,
          totalJobs: companyJobs.length,
          activeJobs: companyJobs.length,
          jobs: companyJobs,
        ));
      });
    }

    // Sort by active jobs count
    companies.sort((a, b) => b.activeJobs.compareTo(a.activeJobs));

    // Filtered
    final filtered = companies.where((c) {
      return c.name.toLowerCase().contains(_searchTerm.toLowerCase()) ||
          c.industry.toLowerCase().contains(_searchTerm.toLowerCase());
    }).toList();

    // Paginated
    final totalPages = (filtered.length / companiesPerPage).ceil();
    final pageToUse = _currentPage > totalPages ? (totalPages == 0 ? 1 : totalPages) : _currentPage;
    final startIndex = (pageToUse - 1) * companiesPerPage;
    final endIndex = (startIndex + companiesPerPage) > filtered.length
        ? filtered.length
        : (startIndex + companiesPerPage);

    final paginatedCompanies =
        (startIndex < filtered.length) ? filtered.subList(startIndex, endIndex) : <Company>[];

    final totalActiveJobs = companies.fold<int>(0, (sum, item) => sum + item.activeJobs);

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      body: _selectedCompany != null
          ? _buildSelectedCompanyView(_selectedCompany!, provider)
          : SafeArea(
              child: ListView(
                padding: const EdgeInsets.all(16.0),
                children: [
                  // Employer directory header
                  Row(
                    children: const [
                      Icon(Icons.business, color: Color(0xFF3B82F6), size: 16),
                      SizedBox(width: 8),
                      Text(
                        'EMPLOYER DIRECTORY',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.bold,
                          color: Color(0xFF3B82F6),
                          letterSpacing: 1.5,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'Companies & Employers',
                    style: TextStyle(
                      fontSize: 32,
                      fontWeight: FontWeight.black,
                      color: Colors.white,
                    ),
                  ),
                  const SizedBox(height: 6),
                  const Text(
                    'Browse top companies actively hiring verified professionals.',
                    style: TextStyle(fontSize: 15, color: Color(0xFF94A3B8)),
                  ),
                  const SizedBox(height: 12),

                  Row(
                    children: [
                      Row(
                        children: [
                          const Icon(Icons.business, color: Color(0xFF3B82F6), size: 16),
                          const SizedBox(width: 6),
                          Text(
                            '${companies.length} Companies',
                            style: const TextStyle(color: Color(0xFFE2E8F0), fontWeight: FontWeight.bold, fontSize: 13),
                          ),
                        ],
                      ),
                      const SizedBox(width: 16),
                      Row(
                        children: [
                          const Icon(Icons.work, color: Color(0xFF10B981), size: 16),
                          const SizedBox(width: 6),
                          Text(
                            '$totalActiveJobs Active Jobs',
                            style: const TextStyle(color: Color(0xFFE2E8F0), fontWeight: FontWeight.bold, fontSize: 13),
                          ),
                        ],
                      ),
                      const SizedBox(width: 16),
                      if (isCompaniesLoading)
                        Row(
                          children: const [
                            SizedBox(
                              width: 12,
                              height: 12,
                              child: CircularProgressIndicator(color: Color(0xFF3B82F6), strokeWidth: 1.5),
                            ),
                            SizedBox(width: 6),
                            Text('Syncing...', style: TextStyle(fontSize: 11, color: Color(0xFF94A3B8))),
                          ],
                        )
                      else if (liveCompanies.isNotEmpty)
                        Row(
                          children: [
                            Container(
                              width: 8,
                              height: 8,
                              decoration: const BoxDecoration(color: Color(0xFF10B981), shape: BoxShape.circle),
                            ),
                            const SizedBox(width: 6),
                            const Text('Live Sync', style: TextStyle(fontSize: 11, color: Color(0xFF10B981), fontWeight: FontWeight.bold)),
                          ],
                        ),
                    ],
                  ),
                  const SizedBox(height: 16),

                  // Search bar
                  TextField(
                    onChanged: (val) {
                      setState(() {
                        _searchTerm = val;
                        _currentPage = 1;
                      });
                    },
                    decoration: InputDecoration(
                      hintText: 'Search ${companies.length} companies...',
                      hintStyle: const TextStyle(color: Color(0xFF64748B)),
                      prefixIcon: const Icon(Icons.search, color: Color(0xFF64748B)),
                      filled: true,
                      fillColor: const Color(0xFF1E293B).withOpacity(0.3),
                      focusedBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(16),
                        borderSide: const BorderSide(color: Color(0xFF3B82F6), width: 1.5),
                      ),
                      enabledBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(16),
                        borderSide: const BorderSide(color: Color(0xFF334155)),
                      ),
                    ),
                    style: const TextStyle(color: Colors.white),
                  ),
                  const SizedBox(height: 16),

                  if (paginatedCompanies.isEmpty)
                    const Padding(
                      padding: EdgeInsets.symmetric(vertical: 48.0),
                      child: Center(
                        child: Text(
                          'No companies found matching your search.',
                          style: TextStyle(color: Color(0xFF64748B), fontSize: 14),
                        ),
                      ),
                    )
                  else ...[
                    // Company list
                    GridView.builder(
                      shrinkWrap: true,
                      physics: const NeverScrollableScrollPhysics(),
                      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                        crossAxisCount: 1,
                        mainAxisSpacing: 12,
                        childAspectRatio: 3.5,
                      ),
                      itemCount: paginatedCompanies.length,
                      itemBuilder: (context, index) {
                        final comp = paginatedCompanies[index];
                        final isLogoUrl = comp.logoResName?.startsWith('http') ?? false;

                        return Card(
                          color: const Color(0xFF1E293B).withOpacity(0.4),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(16),
                            side: BorderSide(color: const Color(0xFF334155).withOpacity(0.3)),
                          ),
                          child: InkWell(
                            onTap: () {
                              setState(() {
                                _selectedCompany = comp;
                              });
                            },
                            borderRadius: BorderRadius.circular(16),
                            child: Padding(
                              padding: const EdgeInsets.all(12.0),
                              child: Row(
                                children: [
                                  // Logo
                                  Container(
                                    width: 48,
                                    height: 48,
                                    decoration: BoxDecoration(
                                      borderRadius: BorderRadius.circular(10),
                                      gradient: const LinearGradient(colors: [Color(0xFF3B82F6), Color(0xFF8B5CF6)]),
                                    ),
                                    clipBehavior: Clip.antiAlias,
                                    alignment: Alignment.center,
                                    child: isLogoUrl
                                        ? CachedNetworkImage(
                                            imageUrl: comp.logoResName!,
                                            fit: BoxFit.cover,
                                            errorWidget: (context, url, err) => Text(
                                              comp.name.substring(0, 2).toUpperCase(),
                                              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                                            ),
                                          )
                                        : Text(
                                            comp.name.substring(0, 2).toUpperCase(),
                                            style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                                          ),
                                  ),
                                  const SizedBox(width: 12),
                                  Expanded(
                                    child: Column(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      mainAxisAlignment: MainAxisAlignment.center,
                                      children: [
                                        Text(
                                          comp.name,
                                          style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 15),
                                          maxLines: 1,
                                          overflow: TextOverflow.ellipsis,
                                        ),
                                        Text(
                                          comp.industry,
                                          style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12),
                                        ),
                                      ],
                                    ),
                                  ),
                                  Column(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    crossAxisAlignment: CrossAxisAlignment.end,
                                    children: [
                                      Container(
                                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                        decoration: BoxDecoration(
                                          color: const Color(0xFF3B82F6).withOpacity(0.12),
                                          borderRadius: BorderRadius.circular(6),
                                        ),
                                        child: Text(
                                          '${comp.activeJobs} Jobs',
                                          style: const TextStyle(color: Color(0xFF3B82F6), fontWeight: FontWeight.bold, fontSize: 11),
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

                    const SizedBox(height: 24),

                    // Pagination row
                    if (totalPages > 1)
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          IconButton(
                            icon: const Icon(Icons.chevron_left, color: Color(0xFF3B82F6)),
                            onPressed: pageToUse > 1
                                ? () {
                                    setState(() {
                                      _currentPage = pageToUse - 1;
                                    });
                                  }
                                : null,
                          ),
                          Text(
                            'Page $pageToUse of $totalPages',
                            style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 13),
                          ),
                          IconButton(
                            icon: const Icon(Icons.chevron_right, color: Color(0xFF3B82F6)),
                            onPressed: pageToUse < totalPages
                                ? () {
                                    setState(() {
                                      _currentPage = pageToUse + 1;
                                    });
                                  }
                                : null,
                          ),
                        ],
                      ),
                  ],
                ],
              ),
            ),
    );
  }

  Widget _buildSelectedCompanyView(Company company, JobsProvider provider) {
    final isLogoUrl = company.logoUrl?.startsWith('http') ?? false;

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () {
            setState(() {
              _selectedCompany = null;
            });
          },
        ),
        title: Text(company.name),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16.0),
        children: [
          // Company Profile Header card
          Card(
            color: const Color(0xFF1E293B),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(24),
              side: BorderSide(color: const Color(0xFF334155).withOpacity(0.5)),
            ),
            child: Padding(
              padding: const EdgeInsets.all(20.0),
              child: Row(
                children: [
                  Container(
                    width: 64,
                    height: 64,
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(14),
                      gradient: const LinearGradient(colors: [Color(0xFF3B82F6), Color(0xFF8B5CF6)]),
                    ),
                    clipBehavior: Clip.antiAlias,
                    alignment: Alignment.center,
                    child: isLogoUrl
                        ? CachedNetworkImage(
                            imageUrl: company.logoUrl!,
                            fit: BoxFit.cover,
                            errorWidget: (context, url, err) => Text(
                              company.name.substring(0, 2).toUpperCase(),
                              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 20),
                            ),
                          )
                        : Text(
                            company.name.substring(0, 2).toUpperCase(),
                            style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 20),
                          ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: const [
                            Text('Verified', style: TextStyle(color: Color(0xFF3B82F6), fontWeight: FontWeight.bold, fontSize: 13)),
                            SizedBox(width: 4),
                            Icon(Icons.verified, color: Color(0xFF3B82F6), size: 14),
                          ],
                        ),
                        Text(
                          company.name,
                          style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 18),
                        ),
                        Text(
                          company.industry,
                          style: const TextStyle(color: Color(0xFFA78BFA), fontSize: 13, fontWeight: FontWeight.w600),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          // Business Details card
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
                    children: const [
                      Icon(Icons.info_outline, color: Color(0xFF3B82F6), size: 16),
                      SizedBox(width: 8),
                      Text('BUSINESS INFO', style: TextStyle(color: Color(0xFF3B82F6), fontSize: 11, fontWeight: FontWeight.bold)),
                    ],
                  ),
                  const SizedBox(height: 16),
                  if (company.website.isNotEmpty)
                    _buildMetaRow('Website', company.website.replaceAll('https://', ''), isLink: true, url: company.website),
                  _buildMetaRow('Industry', company.industry),
                  if (company.foundedYear != null) _buildMetaRow('Founded', company.foundedYear!),
                  if (company.employeeCount != null) _buildMetaRow('Employees', company.employeeCount!),
                  if (company.locality != null) _buildMetaRow('City/HQ', company.locality!),
                  if (company.country != null) _buildMetaRow('Country', company.country == 'TZ' ? '🇹🇿 Tanzania' : company.country!),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          // About Description
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
                  Text(
                    'ABOUT ${company.name.toUpperCase()}',
                    style: const TextStyle(color: Color(0xFFF59E0B), fontSize: 11, fontWeight: FontWeight.bold, fontFamily: 'monospace'),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    company.description,
                    style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 14, height: 1.5),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          // Job Openings
          Row(
            children: [
              Container(
                width: 4,
                height: 18,
                decoration: BoxDecoration(
                  color: const Color(0xFF3B82F6),
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              const SizedBox(width: 8),
              Text(
                'Job Openings (${company.jobs.length})',
                style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.white),
              ),
            ],
          ),
          const SizedBox(height: 12),

          if (company.jobs.isEmpty)
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 24.0),
              child: Center(
                child: Text(
                  'No job listings available for this company right now.',
                  style: TextStyle(color: Color(0xFF64748B), fontSize: 14),
                ),
              ),
            )
          else
            Column(
              children: company.jobs.map((job) {
                return Padding(
                  padding: const EdgeInsets.only(bottom: 12.0),
                  child: Card(
                    color: const Color(0xFF1E293B).withOpacity(0.5),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(16),
                      side: BorderSide(color: const Color(0xFF334155).withOpacity(0.5)),
                    ),
                    child: InkWell(
                      onTap: () {
                        Navigator.pushNamed(context, 'job_detail/${job.id}');
                      },
                      borderRadius: BorderRadius.circular(16),
                      child: Padding(
                        padding: const EdgeInsets.all(18.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Row(
                                  children: [
                                    Container(
                                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                      decoration: BoxDecoration(
                                        color: const Color(0xFF3B82F6).withOpacity(0.1),
                                        borderRadius: BorderRadius.circular(4),
                                      ),
                                      child: const Text(
                                        'Active',
                                        style: TextStyle(color: Color(0xFF3B82F6), fontSize: 9, fontWeight: FontWeight.bold),
                                      ),
                                    ),
                                    const SizedBox(width: 8),
                                    Text(
                                      job.type,
                                      style: const TextStyle(color: Color(0xFF64748B), fontSize: 10, fontWeight: FontWeight.bold),
                                    ),
                                  ],
                                ),
                              ],
                            ),
                            const SizedBox(height: 8),
                            Text(
                              job.title,
                              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 15),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                            const SizedBox(height: 10),
                            Row(
                              children: [
                                const Icon(Icons.place, color: Color(0xFF64748B), size: 12),
                                const SizedBox(width: 4),
                                Text(job.location, style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 11)),
                                const SizedBox(width: 16),
                                const Icon(Icons.monetization_on, color: Color(0xFF10B981), size: 12),
                                const SizedBox(width: 4),
                                Text(job.salary, style: const TextStyle(color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.bold)),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                );
              }).toList(),
            ),
        ],
      ),
    );
  }

  Widget _buildMetaRow(String label, String value, {bool isLink = false, String? url}) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(color: Color(0xFF64748B), fontSize: 13)),
          isLink
              ? InkWell(
                  onTap: () => _launchUrl(url!),
                  child: Text(
                    value,
                    style: const TextStyle(color: Color(0xFF3B82F6), fontSize: 13, decoration: TextDecoration.underline),
                  ),
                )
              : Text(value, style: const TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }
}
