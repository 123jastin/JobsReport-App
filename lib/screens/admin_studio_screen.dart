import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';

class AdminStudioScreen extends StatefulWidget {
  const AdminStudioScreen({super.key});

  @override
  State<AdminStudioScreen> createState() => _AdminStudioScreenState();
}

class _AdminStudioScreenState extends State<AdminStudioScreen> {
  bool _isAdmin = false;
  final TextEditingController _passcodeController = TextEditingController();
  String _errorMessage = '';

  @override
  void dispose() {
    _passcodeController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (!_isAdmin) {
      return Scaffold(
        backgroundColor: const Color(0xFF0F172A),
        body: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(
                  Icons.lock,
                  color: Color(0xFFEF4444),
                  size: 64,
                ),
                const SizedBox(height: 16),
                const Text(
                  'ADMIN ESCORT ACCESS',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 20,
                    fontWeight: FontWeight.black,
                    fontFamily: 'monospace',
                    letterSpacing: 1.0,
                  ),
                ),
                const SizedBox(height: 4),
                const Text(
                  'Requires verified personnel authorization credentials.',
                  style: TextStyle(color: Color(0xFF94A3B8), fontSize: 13),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 24),
                TextField(
                  controller: _passcodeController,
                  obscureText: true,
                  style: const TextStyle(color: Colors.white, fontSize: 14),
                  decoration: InputDecoration(
                    labelText: 'System Access Passcode',
                    labelStyle: const TextStyle(color: Color(0xFF94A3B8)),
                    focusedBorder: const OutlineInputBorder(
                      borderSide: BorderSide(color: Color(0xFF10B981)),
                    ),
                    enabledBorder: const OutlineInputBorder(
                      borderSide: BorderSide(color: Color(0xFF334155)),
                    ),
                  ),
                ),
                if (_errorMessage.isNotEmpty) ...[
                  const SizedBox(height: 8),
                  Text(
                    _errorMessage,
                    style: const TextStyle(color: Color(0xFFEF4444), fontSize: 12, fontWeight: FontWeight.bold),
                  ),
                ],
                const SizedBox(height: 20),
                SizedBox(
                  width: double.infinity,
                  height: 50,
                  child: ElevatedButton(
                    onPressed: () {
                      final code = _passcodeController.text.trim().toLowerCase();
                      if (code == 'admin' || code == '12345') {
                        setState(() {
                          _isAdmin = true;
                          _errorMessage = '';
                        });
                        _passcodeController.clear();
                      } else {
                        setState(() {
                          _errorMessage = 'ACCESS DENIED: INVALID SIGNATURE.';
                        });
                      }
                    },
                    style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFFEF4444)),
                    child: const Text(
                      'DECRYPT KEY & UNLOCK',
                      style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                    ),
                  ),
                ),
                const SizedBox(height: 32),
                const Text(
                  'HINT: ENTER "admin" OR "12345" TO UNLOCK THE INGESTION ENGINE',
                  style: TextStyle(fontFamily: 'monospace', color: Color(0xFF64748B), fontSize: 9),
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),
        ),
      );
    }

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      body: SafeArea(
        child: AdminDashboardConsole(
          onLock: () {
            setState(() {
              _isAdmin = false;
            });
          },
        ),
      ),
    );
  }
}

class AdminDashboardConsole extends StatefulWidget {
  final VoidCallback onLock;

  const AdminDashboardConsole({
    super.key,
    required this.onLock,
  });

  @override
  State<AdminDashboardConsole> createState() => _AdminDashboardConsoleState();
}

class _AdminDashboardConsoleState extends State<AdminDashboardConsole> {
  bool _isIngesting = true;
  final List<String> _logs = [];
  Timer? _logTimer;
  final ScrollController _scrollController = ScrollController();
  final Random _random = Random();

  final List<String> _baseLogs = const [
    '⚡ SYSTEM: Ingestion worker online. Scanning targets...',
    '🔌 WEBHOOK: Incoming sync from jobsreport.online (IP: 192.168.10.45)...',
    '🗄️ DATABASE: Connecting to Cloud Spanner partition...',
    '🔎 SCRAPER: Fetching SwahiliTech Dar es Salaam endpoints...',
    '📥 INGESTED: "Mobile Developer" -> Deduped (MD5 matching ok).',
    '📈 METRICS: Recalculated index density: +3.4% engineering.',
    '🔑 AUTH: Sync signed with Admin Swahili SSL Certificate.',
    '📊 ANALYSIS: Parsing resume keywords using AI Studio NLP nodes.',
    '🤖 GEMINI: Optimizing cover letter engine weights.',
    '⚡ SYSTEM: Sync completed successfully. Sitting in standby.'
  ];

  @override
  void initState() {
    super.initState();
    _startLogSimulation();
  }

  void _startLogSimulation() {
    _logTimer = Timer.periodic(const Duration(seconds: 2), (timer) {
      if (_isIngesting && mounted) {
        setState(() {
          final logText = _baseLogs[_random.nextInt(_baseLogs.length)];
          final timestamp = DateTime.now().millisecondsSinceEpoch % 100000;
          _logs.add('[$timestamp] $logText');
          if (_logs.length > 50) {
            _logs.removeAt(0);
          }
        });
        _scrollToBottom();
      }
    });
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  void dispose() {
    _logTimer?.cancel();
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      children: [
        // Console Header
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Row(
              children: const [
                Icon(Icons.terminal, color: Color(0xFF10B981), size: 24),
                SizedBox(width: 8),
                Text(
                  'INGESTION CONSOLE v2.4',
                  style: TextStyle(
                    fontFamily: 'monospace',
                    fontWeight: FontWeight.bold,
                    fontSize: 14,
                    color: Colors.white,
                  ),
                ),
              ],
            ),
            SizedBox(
              height: 36,
              child: ElevatedButton(
                onPressed: widget.onLock,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF475569),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                ),
                child: const Text('LOCK ENGINE', style: TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.bold)),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),

        // System Control Row
        Container(
          padding: const EdgeInsets.all(12.0),
          decoration: BoxDecoration(
            color: const Color(0xFF1E293B),
            borderRadius: BorderRadius.circular(8),
            border: Border.all(color: const Color(0xFF334155)),
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'INGESTION ENGINE',
                    style: TextStyle(
                      fontFamily: 'monospace',
                      color: Color(0xFF94A3B8),
                      fontSize: 10,
                    ),
                  ),
                  Text(
                    _isIngesting ? 'STATUS: RUNNING' : 'STATUS: PAUSED',
                    style: TextStyle(
                      fontFamily: 'monospace',
                      fontWeight: FontWeight.bold,
                      color: _isIngesting ? const Color(0xFF10B981) : const Color(0xFFF59E0B),
                      fontSize: 13,
                    ),
                  ),
                ],
              ),
              IconButton(
                onPressed: () {
                  setState(() {
                    _isIngesting = !_isIngesting;
                  });
                },
                icon: Icon(
                  _isIngesting ? Icons.stop : Icons.play_arrow,
                  color: _isIngesting ? const Color(0xFFEF4444) : const Color(0xFF10B981),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),

        const Text(
          'LIVE TELEMETRY STREAM',
          style: TextStyle(fontFamily: 'monospace', color: Color(0xFF64748B), fontSize: 10),
        ),
        const SizedBox(height: 6),

        // Terminal Log Container
        Expanded(
          child: Container(
            width: double.infinity,
            padding: const EdgeInsets.all(12.0),
            decoration: BoxDecoration(
              color: Colors.black,
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: const Color(0xFF334155)),
            ),
            child: _logs.isEmpty
                ? const Text(
                    'Initializing terminal socket...\nListening for secure triggers on webhook 3000...',
                    style: TextStyle(
                      fontFamily: 'monospace',
                      color: Color(0xFF10B981),
                      fontSize: 12,
                      height: 1.4,
                    ),
                  )
                : ListView.builder(
                    controller: _scrollController,
                    itemCount: _logs.length,
                    itemBuilder: (context, idx) {
                      return Padding(
                        padding: const EdgeInsets.symmetric(vertical: 2.0),
                        child: Text(
                          _logs[idx],
                          style: const TextStyle(
                            fontFamily: 'monospace',
                            color: Color(0xFF10B981),
                            fontSize: 11,
                            height: 1.4,
                          ),
                        ),
                      );
                    },
                  ),
          ),
        ),
      ],
    );
  }
}
