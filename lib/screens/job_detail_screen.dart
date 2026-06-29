import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_html/flutter_html.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:share_plus/share_plus.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../providers/jobs_provider.dart';
import '../models/models.dart';

class JobDetailScreen extends StatefulWidget {
  final int jobId;

  const JobDetailScreen({super.key, required this.jobId});

  @override
  State<JobDetailScreen> createState() => _JobDetailScreenState();
}

class _JobDetailScreenState extends State<JobDetailScreen> {
  Timer? _redirectTimer;
  int _timeLeft = 10;
  double _animatedProgress = 0.0;
  bool _showRedirectDialog = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final provider = Provider.of<JobsProvider>(context, listen: false);
      // Find the job in local memory to fetch by remoteId
      final job = provider.allJobs.firstWhere(
        (j) => j.id == widget.jobId,
        orElse: () => JobEntity(
          id: widget.jobId,
          title: '',
          company: '',
          logoResName: '',
          location: '',
          category: '',
          remoteId: widget.jobId.toString(),
        ),
      );
      provider.fetchJobDetail(job.remoteId);
    });
  }

  @override
  void dispose() {
    _redirectTimer?.cancel();
    super.dispose();
  }

  void _startRedirectTimer(BuildContext context, String jobUrl, JobEntity job) {
    _timeLeft = 10;
    _animatedProgress = 0.0;
    _showRedirectDialog = true;

    final provider = Provider.of<JobsProvider>(context, listen: false);

    _redirectTimer = Timer.periodic(const Duration(milliseconds: 100), (timer) {
      setState(() {
        _animatedProgress += 0.01;
        _timeLeft = 10 - (_animatedProgress * 10).floor();
        if (_animatedProgress >= 1.0) {
          _animatedProgress = 1.0;
          _timeLeft = 0;
          timer.cancel();
          _showRedirectDialog = false;

          // Save application telemetry
          provider.setAppliedStatus(job.remoteId, 'Applied', 'Auto-redirected via Telemetry Bridge');

          // Launch URL
          _launchUrl(jobUrl);
        }
      });
    });

    _showRedirect(context, jobUrl, job);
  }

  void _showRedirect(BuildContext context, String jobUrl, JobEntity job) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setDialogState) {
            return AlertDialog(
              backgroundColor: const Color(0xFF1E293B),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              title: Row(
                children: const [
                  Icon(Icons.auto_awesome, color: Color(0xFF3B82F6)),
                  SizedBox(width: 8),
                  Text('Telemetry Bridge', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 18)),
                ],
              ),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Redirecting you to the official recruitment portal...',
                    style: TextStyle(color: Color(0xFFCBD5E1), fontSize: 14),
                  ),
                  const SizedBox(height: 16),
                  LinearProgressIndicator(
                    value: _animatedProgress,
                    backgroundColor: const Color(0xFF334155),
                    color: const Color(0xFF3B82F6),
                    minHeight: 6,
                  ),
                  const SizedBox(height: 12),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        'Redirecting in $_timeLeft seconds',
                        style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12, fontFamily: 'monospace'),
                      ),
                      TextButton(
                        onPressed: () {
                          _redirectTimer?.cancel();
                          Navigator.pop(context);
                          setState(() {
                            _showRedirectDialog = false;
                          });
                        },
                        child: const Text('CANCEL', style: TextStyle(color: Color(0xFFEF4444), fontWeight: FontWeight.bold)),
                      ),
                    ],
                  ),
                ],
              ),
            );
          },
        );
      },
    ).then((_) {
      _redirectTimer?.cancel();
    });
  }

  Future<void> _launchUrl(String url) async {
    final Uri uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Could not open link: $url')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<JobsProvider>(context);

    // Find our current job representation
    final JobEntity? job = provider.allJobs.firstWhere(
      (j) => j.id == widget.jobId,
      orElse: () => JobEntity(
        id: widget.jobId,
        title: 'Loading Job...',
        company: '',
        logoResName: '',
        location: '',
        salary: '',
        type: '',
        workplace: '',
        datePosted: '',
        description: '',
        requirements: '',
        benefits: '',
        category: '',
        companyWebsite: '',
        active: true,
        remoteId: widget.jobId.toString(),
      ),
    );

    final liveDetail = provider.liveJobDetail;
    final isSyncing = provider.isJobDetailLoading;

    final bool isExpired = !(liveDetail?.active ?? job?.active ?? true);

    final String jobUrl = liveDetail?.url ?? job?.companyWebsite ?? '';
    final String? whatsappNumber = liveDetail?.whatsapp_number;
    final String? applicationInstructions = liveDetail?.application_instructions;

    final bool hasWhatsApp = whatsappNumber != null && whatsappNumber.trim().length > 6;
    final bool hasInstructions = applicationInstructions != null && applicationInstructions.trim().isNotEmpty;
    final bool isEmailLink = jobUrl.startsWith('mailto:');

    // Attachments Mapping
    final List<MockAttachment> attachments = [];
    if (liveDetail?.images != null) {
      for (final img in liveDetail!.images!) {
        final type = (img.type == 'pdf' || (img.name?.toLowerCase().endsWith('.pdf') ?? false)) ? 'pdf' : 'image';
        attachments.add(MockAttachment(
          name: img.name ?? 'Attachment',
          type: type,
          description: img.caption ?? img.seoDescription ?? '',
          url: img.url,
          thumbnail: img.thumbnail ?? img.url,
        ));
      }
    }

    // Similar Active Jobs
    final List<JobEntity> relatedJobs = [];
    if (liveDetail?.relatedJobs != null) {
      relatedJobs.addAll(liveDetail!.relatedJobs!.map((rj) => JobEntity.fromRemote(rj)));
    } else if (job != null) {
      relatedJobs.addAll(provider.allJobs.where((j) => j.category == job.category && j.remoteId != job.remoteId).take(3));
    }

    // Company Description lookup
    final RemoteCompany? matchedCompany = provider.liveCompanies.firstWhere(
      (c) => c.name.toLowerCase() == job?.company.toLowerCase(),
      orElse: () => RemoteCompany(id: '', name: ''),
    );

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: Row(
          children: [
            const Text(
              'Market Vacancy',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
            ),
            const SizedBox(width: 8),
            if (isSyncing)
              Row(
                children: const [
                  SizedBox(
                    width: 10,
                    height: 10,
                    child: CircularProgressIndicator(color: Color(0xFF3B82F6), strokeWidth: 1.2),
                  ),
                  SizedBox(width: 4),
                  Text('Syncing...', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 11)),
                ],
              )
            else if (liveDetail != null)
              Row(
                children: [
                  Container(
                    width: 6,
                    height: 6,
                    decoration: const BoxDecoration(color: Color(0xFF10B981), shape: BoxShape.circle),
                  ),
                  const SizedBox(width: 4),
                  const Text('Live Sync', style: TextStyle(color: Color(0xFF10B981), fontSize: 11, fontWeight: FontWeight.bold)),
                ],
              ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.share, color: Color(0xFF94A3B8)),
            onPressed: () {
              if (job != null) {
                Share.share('Check out this job opportunity: ${job.title} at ${job.company} on JobsReport.online');
              }
            },
          ),
          IconButton(
            icon: Icon(
              (job?.isBookmarked ?? false) ? Icons.bookmark : Icons.bookmark_border,
              color: (job?.isBookmarked ?? false) ? const Color(0xFF3B82F6) : const Color(0xFF94A3B8),
            ),
            onPressed: () {
              if (job != null) {
                provider.toggleBookmark(job.remoteId);
              }
            },
          ),
        ],
      ),
      bottomNavigationBar: Container(
        decoration: const BoxDecoration(
          color: Color(0xFF0F172A),
          border: Border(top: BorderSide(color: Color(0xFF1E293B))),
        ),
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            if (hasInstructions && !isExpired)
              Container(
                width: double.infinity,
                margin: const EdgeInsets.only(bottom: 8.0),
                padding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 10.0),
                decoration: BoxDecoration(
                  color: const Color(0xFF3B82F6).withOpacity(0.05),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: const Color(0xFF3B82F6).withOpacity(0.1)),
                ),
                child: Text(
                  '📋 How to Apply: $applicationInstructions',
                  style: const TextStyle(color: Color(0xFF93C5FD), fontSize: 11, height: 1.4),
                ),
              ),
            if (jobUrl.isNotEmpty && !isExpired && !hasWhatsApp)
              Padding(
                padding: const EdgeInsets.only(bottom: 8.0),
                child: Text(
                  isEmailLink ? '📧 ${jobUrl.replaceAll('mailto:', '')}' : '🔗 $jobUrl',
                  style: const TextStyle(color: Color(0xFF64748B), fontSize: 10),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            if (hasWhatsApp && !isExpired)
              ElevatedButton.icon(
                onPressed: () {
                  final cleanNum = whatsappNumber!.replaceAll(RegExp(r'\D'), '');
                  final text = 'Hello, I am interested in the ${job?.title} position at ${job?.company}. Please share more details.';
                  _launchUrl('https://wa.me/$cleanNum?text=${Uri.encodeComponent(text)}');
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF16A34A),
                  foregroundColor: Colors.white,
                  minimumSize: const Size(double.infinity, 50),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
                icon: const Icon(Icons.chat),
                label: const Text('Apply via WhatsApp', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
              )
            else if (isExpired)
              ElevatedButton(
                onPressed: null,
                style: ElevatedButton.styleFrom(
                  disabledBackgroundColor: const Color(0xFFEF4444).withOpacity(0.1),
                  disabledForegroundColor: const Color(0xFFF87171),
                  minimumSize: const Size(double.infinity, 50),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
                child: const Text('🚫 Application Closed', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
              )
            else if (jobUrl.isNotEmpty)
              ElevatedButton.icon(
                onPressed: () {
                  if (job != null) {
                    _startRedirectTimer(context, jobUrl, job);
                  }
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF2563EB),
                  foregroundColor: Colors.white,
                  minimumSize: const Size(double.infinity, 50),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
                icon: Icon(isEmailLink ? Icons.email : Icons.arrow_forward),
                label: Text(
                  isEmailLink ? 'Send Application' : 'Apply Now',
                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                ),
              )
            else
              ElevatedButton(
                onPressed: null,
                style: ElevatedButton.styleFrom(
                  disabledBackgroundColor: Colors.white.withOpacity(0.05),
                  disabledForegroundColor: const Color(0xFF475569),
                  minimumSize: const Size(double.infinity, 50),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
                child: const Text('No application link available', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
              ),
          ],
        ),
      ),
      body: job == null || job.title == 'Loading Job...'
          ? const Center(child: CircularProgressIndicator(color: Color(0xFF3B82F6)))
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // 1. Header Card
                  _buildHeaderCard(job, isExpired),
                  const SizedBox(height: 16),

                  // 2. Stay Safe online
                  _buildStaySafeWarning(),
                  const SizedBox(height: 16),

                  // 3. About Company Card
                  _buildCompanyCard(job, matchedCompany),
                  const SizedBox(height: 16),

                  // 4. Attachments Section
                  if (attachments.isNotEmpty) ...[
                    _buildAttachmentsSection(attachments),
                    const SizedBox(height: 16),
                  ],

                  // 5. Job Description (HTML parser with correct ul/ol bullet layouts)
                  _buildDetailSection('Job Description', job.description),
                  const SizedBox(height: 16),

                  // 6. Requirements (HTML parser with correct ul/ol bullet layouts)
                  if (job.requirements.isNotEmpty) ...[
                    _buildDetailSection('Requirements', job.requirements),
                    const SizedBox(height: 16),
                  ],

                  // 7. Benefits (HTML parser with correct ul/ol bullet layouts)
                  if (job.benefits.isNotEmpty) ...[
                    _buildDetailSection('Benefits & Perks', job.benefits),
                    const SizedBox(height: 16),
                  ],

                  // 8. Similar Active Jobs
                  if (relatedJobs.isNotEmpty) ...[
                    _buildRelatedJobsSection(relatedJobs, provider),
                  ],
                ],
              ),
            ),
    );
  }

  Widget _buildHeaderCard(JobEntity job, bool isExpired) {
    final isUrl = job.logoResName.startsWith('http://') || job.logoResName.startsWith('https://');

    return Card(
      color: const Color(0xFF1E293B).withOpacity(0.4),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(color: const Color(0xFF334155).withOpacity(0.4)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            Container(
              width: 56,
              height: 56,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(12),
                gradient: const LinearGradient(
                  colors: [Color(0xFF3B82F6), Color(0xFF8B5CF6)],
                ),
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
                        style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 18),
                      ),
                    )
                  : Text(
                      job.company.isNotEmpty ? job.company.substring(0, 2).toUpperCase() : 'JR',
                      style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 18),
                    ),
            ),
            const SizedBox(height: 12),
            Text(
              job.title,
              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.black, fontSize: 20),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 4),
            Text(
              job.company,
              style: const TextStyle(color: Color(0xFF3B82F6), fontWeight: FontWeight.bold, fontSize: 15),
              textAlign: TextAlign.center,
            ),
            if (isExpired) ...[
              const SizedBox(height: 8),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
                decoration: BoxDecoration(
                  color: const Color(0xFFEF4444).withOpacity(0.15),
                  borderRadius: BorderRadius.circular(6),
                ),
                child: const Text(
                  'EXPIRED',
                  style: TextStyle(color: Color(0xFFEF4444), fontSize: 10, fontWeight: FontWeight.bold),
                ),
              ),
            ],
            const SizedBox(height: 16),
            const Divider(color: Color(0xFF334155)),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                _buildMetaItemCompact(Icons.location_on_outlined, 'LOCATION', job.location),
                _buildMetaItemCompact(Icons.monetization_on_outlined, 'SALARY', job.salary),
                _buildMetaItemCompact(Icons.work_outline, 'ROLE TYPE', '${job.workplace} / ${job.type}'),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMetaItemCompact(IconData icon, String label, String value) {
    return Expanded(
      child: Column(
        children: [
          Icon(icon, color: const Color(0xFF64748B), size: 18),
          const SizedBox(height: 4),
          Text(label, style: const TextStyle(color: Color(0xFF64748B), fontSize: 9, fontWeight: FontWeight.bold)),
          const SizedBox(height: 2),
          Text(
            value,
            style: const TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.bold),
            textAlign: TextAlign.center,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    );
  }

  Widget _buildStaySafeWarning() {
    return Card(
      color: const Color(0xFFEF4444).withOpacity(0.04),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: const Color(0xFFEF4444).withOpacity(0.2)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Row(
          children: [
            const Icon(Icons.shield_outlined, color: Color(0xFFF87171), size: 24),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: const [
                  Text(
                    'STAY SAFE ONLINE',
                    style: TextStyle(color: Color(0xFFF87171), fontWeight: FontWeight.bold, fontSize: 10),
                  ),
                  Text(
                    'Never pay money for application requests or interviews.',
                    style: TextStyle(color: Color(0xFF94A3B8), fontSize: 11),
                  ),
                ],
              ),
            ),
            IconButton(
              icon: const Icon(Icons.flag_outlined, color: Color(0xFFF87171), size: 18),
              onPressed: () {
                _launchUrl('mailto:jjovinatha@gmail.com?subject=Report%20Job&body=Please%20review%20this%20listing.');
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCompanyCard(JobEntity job, RemoteCompany? company) {
    return Card(
      color: const Color(0xFF1E293B).withOpacity(0.2),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(color: const Color(0xFF334155).withOpacity(0.3)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'ABOUT ${job.company.toUpperCase()}',
              style: const TextStyle(
                color: Color(0xFFF59E0B),
                fontWeight: FontWeight.bold,
                fontSize: 10,
                fontFamily: 'monospace',
                letterSpacing: 1.0,
              ),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                const Icon(Icons.business, color: Color(0xFF64748B), size: 16),
                const SizedBox(width: 8),
                Text(job.company, style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.white, fontSize: 15)),
              ],
            ),
            if (job.companyWebsite.isNotEmpty) ...[
              const SizedBox(height: 6),
              InkWell(
                onTap: () => _launchUrl(job.companyWebsite),
                child: Row(
                  children: [
                    const Icon(Icons.language, color: Color(0xFF3B82F6), size: 14),
                    const SizedBox(width: 8),
                    Text(
                      job.companyWebsite.replaceAll('https://', '').replaceAll('www.', ''),
                      style: const TextStyle(color: Color(0xFF3B82F6), fontSize: 12),
                    ),
                  ],
                ),
              ),
            ],
            if (company?.description != null && company!.description!.isNotEmpty) ...[
              const SizedBox(height: 10),
              Text(
                company.description!,
                style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 13, height: 1.5),
                maxLines: 5,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildAttachmentsSection(List<MockAttachment> attachments) {
    return Card(
      color: const Color(0xFF1E293B).withOpacity(0.2),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(color: const Color(0xFF334155).withOpacity(0.3)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'ATTACHMENTS (${attachments.length})',
              style: const TextStyle(
                color: Color(0xFF64748B),
                fontWeight: FontWeight.bold,
                fontSize: 10,
                fontFamily: 'monospace',
                letterSpacing: 1.0,
              ),
            ),
            const SizedBox(height: 12),
            Column(
              children: attachments.map((attachment) {
                final isPdf = attachment.type == 'pdf';

                return Padding(
                  padding: const EdgeInsets.only(bottom: 8.0),
                  child: InkWell(
                    onTap: () {
                      if (attachment.url != null) {
                        _launchUrl(attachment.url!);
                      }
                    },
                    borderRadius: BorderRadius.circular(10),
                    child: Container(
                      padding: const EdgeInsets.all(12.0),
                      decoration: BoxDecoration(
                        color: const Color(0xFF1E293B).withOpacity(0.6),
                        borderRadius: BorderRadius.circular(10),
                        border: Border.all(color: const Color(0xFF334155).withOpacity(0.4)),
                      ),
                      child: Row(
                        children: [
                          Container(
                            width: 36,
                            height: 36,
                            decoration: BoxDecoration(
                              color: isPdf ? const Color(0xFFEF4444).withOpacity(0.12) : const Color(0xFF3B82F6).withOpacity(0.12),
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: Icon(
                              isPdf ? Icons.picture_as_pdf : Icons.image,
                              color: isPdf ? const Color(0xFFF87171) : const Color(0xFF60A5FA),
                              size: 18,
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  attachment.name,
                                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 12),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                                Text(
                                  attachment.description,
                                  style: const TextStyle(color: Color(0xFF64748B), fontSize: 10),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ],
                            ),
                          ),
                          IconButton(
                            icon: const Icon(Icons.download, color: Color(0xFF94A3B8), size: 16),
                            onPressed: () {
                              if (attachment.url != null) {
                                _launchUrl(attachment.url!);
                              }
                            },
                          ),
                        ],
                      ),
                    ),
                  ),
                );
              }).toList(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDetailSection(String title, String htmlContent) {
    return Column(
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
            Text(
              title,
              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Card(
          color: const Color(0xFF1E293B).withOpacity(0.2),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
            side: BorderSide(color: const Color(0xFF334155).withOpacity(0.3)),
          ),
          child: Padding(
            padding: const EdgeInsets.all(12.0),
            child: Html(
              data: htmlContent,
              style: {
                "body": Style(
                  color: const Color(0xFFCBD5E1),
                  fontSize: FontSize(14),
                  lineHeight: const LineHeight(1.6),
                  margin: Margins.zero,
                  padding: HtmlPaddings.zero,
                ),
                "ul": Style(
                  padding: HtmlPaddings.left(20),
                ),
                "ol": Style(
                  padding: HtmlPaddings.left(20),
                ),
                "li": Style(
                  margin: Margins.only(bottom: 4),
                ),
                "b": Style(
                  color: const Color(0xFFF1F5F9),
                  fontWeight: FontWeight.bold,
                ),
                "strong": Style(
                  color: const Color(0xFFF1F5F9),
                  fontWeight: FontWeight.bold,
                ),
              },
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildRelatedJobsSection(List<JobEntity> relatedJobs, JobsProvider provider) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'SIMILAR ACTIVE JOBS',
          style: TextStyle(
            color: Color(0xFF64748B),
            fontWeight: FontWeight.bold,
            fontFamily: 'monospace',
            fontSize: 10,
            letterSpacing: 1.0,
          ),
        ),
        const SizedBox(height: 12),
        Column(
          children: relatedJobs.map((rJob) {
            return Padding(
              padding: const EdgeInsets.only(bottom: 10.0),
              child: Card(
                color: const Color(0xFF1E293B),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                  side: BorderSide(color: const Color(0xFF334155).withOpacity(0.3)),
                ),
                child: InkWell(
                  onTap: () {
                    // Navigate to the related job detail
                    Navigator.pushReplacementNamed(context, 'job_detail/${rJob.remoteId}');
                  },
                  borderRadius: BorderRadius.circular(12),
                  child: Padding(
                    padding: const EdgeInsets.all(14.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Expanded(
                              child: Text(
                                rJob.title,
                                style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ),
                            const Icon(Icons.arrow_outward, color: Color(0xFF64748B), size: 14),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Row(
                          children: [
                            Text(rJob.company, style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),
                            const SizedBox(width: 8),
                            const Text('•', style: TextStyle(color: Color(0xFF475569))),
                            const SizedBox(width: 8),
                            Expanded(
                              child: Text(
                                rJob.location,
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
                ),
              ),
            );
          }).toList(),
        ),
      ],
    );
  }
}

class MockAttachment {
  final String name;
  final String type;
  final String description;
  final String? url;
  final String? thumbnail;

  MockAttachment({
    required this.name,
    required this.type,
    required this.description,
    this.url,
    this.thumbnail,
  });
}
