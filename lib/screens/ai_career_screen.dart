import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../providers/jobs_provider.dart';

class AiCareerScreen extends StatefulWidget {
  const AiCareerScreen({super.key});

  @override
  State<AiCareerScreen> createState() => _AiCareerScreenState();
}

class _AiCareerScreenState extends State<AiCareerScreen> {
  Timer? _tipTimer;
  int _currentTipIndex = 0;

  final List<String> _loadingTips = [
    'Synthesizing job requirements with your skills matrix...',
    'Structuring a highly persuasive cover letter...',
    'Identifying vital technical keywords to beat automated ATS...',
    'Formulating realistic, customized interview practice questions...',
    'Polishing credentials with professional recruiter vocabulary...'
  ];

  @override
  void dispose() {
    _tipTimer?.cancel();
    super.dispose();
  }

  void _startTipRotation() {
    _tipTimer?.cancel();
    _currentTipIndex = 0;
    _tipTimer = Timer.periodic(const Duration(milliseconds: 3500), (timer) {
      if (mounted) {
        setState(() {
          _currentTipIndex = (_currentTipIndex + 1) % _loadingTips.length;
        });
      }
    });
  }

  void _stopTipRotation() {
    _tipTimer?.cancel();
    _tipTimer = null;
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<JobsProvider>(context);
    final aiOutput = provider.aiOutput;
    final isAiLoading = provider.isAiLoading;

    if (isAiLoading) {
      if (_tipTimer == null) {
        _startTipRotation();
      }
    } else {
      _stopTipRotation();
    }

    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: Row(
          children: const [
            Icon(Icons.auto_awesome, color: Color(0xFF3B82F6)),
            SizedBox(width: 8),
            Text('AI Career Advisor', style: TextStyle(fontWeight: FontWeight.bold)),
          ],
        ),
        actions: [
          if (aiOutput.isNotEmpty && !isAiLoading) ...[
            IconButton(
              icon: const Icon(Icons.content_copy, color: Color(0xFF94A3B8)),
              tooltip: 'Copy Draft',
              onPressed: () {
                Clipboard.setData(ClipboardData(text: aiOutput));
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Copied to Clipboard! 📋'),
                    backgroundColor: Color(0xFF10B981),
                  ),
                );
              },
            ),
            IconButton(
              icon: const Icon(Icons.clear, color: Color(0xFFEF4444)),
              tooltip: 'Clear Draft',
              onPressed: () {
                provider.clearAiOutput();
              },
            ),
          ],
        ],
      ),
      body: BoxOrContent(
        isAiLoading: isAiLoading,
        aiOutput: aiOutput,
        tips: _loadingTips,
        tipIndex: _currentTipIndex,
      ),
    );
  }
}

class BoxOrContent extends StatelessWidget {
  final bool isAiLoading;
  final String aiOutput;
  final List<String> tips;
  final int tipIndex;

  const BoxOrContent({
    super.key,
    required this.isAiLoading,
    required this.aiOutput,
    required this.tips,
    required this.tipIndex,
  });

  @override
  Widget build(BuildContext context) {
    if (isAiLoading) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const SizedBox(
                width: 52,
                height: 52,
                child: CircularProgressIndicator(color: Color(0xFF3B82F6), strokeWidth: 4),
              ),
              const SizedBox(height: 24),
              const Text(
                'Drafting with Gemini...',
                style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 18),
              ),
              const SizedBox(height: 16),
              Card(
                color: const Color(0xFF1E293B).withOpacity(0.5),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                  side: BorderSide(color: const Color(0xFF334155).withOpacity(0.5)),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Text(
                    '💡 ${tips[tipIndex]}',
                    style: const TextStyle(color: Color(0xFFE2E8F0), fontSize: 14, height: 1.5),
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
            ],
          ),
        ),
      );
    }

    if (aiOutput.isEmpty) {
      return Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(32.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: const [
              Icon(Icons.auto_awesome, size: 64, color: Color(0xFF475569)),
              SizedBox(height: 24),
              Text(
                'AI Advisor Standing By',
                style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 20),
                textAlign: TextAlign.center,
              ),
              SizedBox(height: 16),
              Text(
                'Navigate to any job\'s detail page and tap one of our AI tools:\n\n• ✍️ Draft Cover Letter\n• 🎯 Tailor Resume\n• 🎤 Prepare Interview\n\nConfigure your Profile first to let the AI draft details tailored specifically to your background!',
                style: TextStyle(color: Color(0xFF94A3B8), fontSize: 14, height: 1.6),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      );
    }

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Card(
        color: const Color(0xFF1E293B),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
          side: BorderSide(color: const Color(0xFF334155).withOpacity(0.5)),
        ),
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                decoration: BoxDecoration(
                  color: const Color(0xFF3B82F6).withOpacity(0.12),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: const [
                    Icon(Icons.auto_awesome, color: Color(0xFF3B82F6), size: 14),
                    SizedBox(width: 6),
                    Text(
                      'Gemini Powered Draft',
                      style: TextStyle(color: Color(0xFF3B82F6), fontWeight: FontWeight.bold, fontSize: 11),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              Text(
                aiOutput,
                style: const TextStyle(color: Color(0xFFE2E8F0), fontSize: 14, height: 1.6),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
