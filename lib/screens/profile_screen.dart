import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/jobs_provider.dart';
import '../models/models.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  final _formKey = GlobalKey<FormState>();

  late TextEditingController _nameController;
  late TextEditingController _titleController;
  late TextEditingController _emailController;
  late TextEditingController _phoneController;
  late TextEditingController _skillsController;
  late TextEditingController _bioController;
  late TextEditingController _experienceController;
  late TextEditingController _educationController;

  @override
  void initState() {
    super.initState();
    final profile = Provider.of<JobsProvider>(context, listen: false).userProfile;

    _nameController = TextEditingController(text: profile.fullName);
    _titleController = TextEditingController(text: profile.targetTitle);
    _emailController = TextEditingController(text: profile.email);
    _phoneController = TextEditingController(text: profile.phone);
    _skillsController = TextEditingController(text: profile.skills);
    _bioController = TextEditingController(text: profile.bio);
    _experienceController = TextEditingController(text: profile.experience);
    _educationController = TextEditingController(text: profile.education);
  }

  @override
  void dispose() {
    _nameController.dispose();
    _titleController.dispose();
    _emailController.dispose();
    _phoneController.dispose();
    _skillsController.dispose();
    _bioController.dispose();
    _experienceController.dispose();
    _educationController.dispose();
    super.dispose();
  }

  void _saveProfile() {
    final provider = Provider.of<JobsProvider>(context, listen: false);
    final updated = UserProfileEntity(
      id: 1,
      fullName: _nameController.text,
      email: _emailController.text,
      phone: _phoneController.text,
      targetTitle: _titleController.text,
      skills: _skillsController.text,
      experience: _experienceController.text,
      education: _educationController.text,
      bio: _bioController.text,
    );
    provider.saveProfile(updated);

    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Profile Synced & Ready for AI Tools! ✅'),
        backgroundColor: Color(0xFF10B981),
        duration: Duration(seconds: 2),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        title: const Text('My Resume Vault', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _saveProfile,
        backgroundColor: const Color(0xFF3B82F6),
        foregroundColor: Colors.white,
        icon: const Icon(Icons.sync),
        label: const Text('Sync Profile', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.only(left: 16, right: 16, top: 16, bottom: 80),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Info Card
              Card(
                color: const Color(0xFF1E293B).withOpacity(0.5),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                  side: BorderSide(color: const Color(0xFF334155).withOpacity(0.5)),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Your Offline Resume Vault',
                        style: TextStyle(color: Color(0xFF3B82F6), fontWeight: FontWeight.bold, fontSize: 16),
                      ),
                      const SizedBox(height: 6),
                      const Text(
                        'Fill in your background to enable instant AI cover letter composing, resume critiquing, and personalized interview practice templates.',
                        style: TextStyle(color: Color(0xFF94A3B8), fontSize: 13, height: 1.5),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 20),

              // Section 1: Personal Details
              _buildSectionHeader(Icons.person_outline, 'Personal Details'),
              const SizedBox(height: 12),

              _buildTextField(
                controller: _nameController,
                label: 'Full Name',
                hint: 'e.g. Alex Mercer',
              ),
              const SizedBox(height: 12),

              _buildTextField(
                controller: _titleController,
                label: 'Target Professional Title',
                hint: 'e.g. Senior Software Engineer',
              ),
              const SizedBox(height: 12),

              Row(
                children: [
                  Expanded(
                    child: _buildTextField(
                      controller: _emailController,
                      label: 'Email Address',
                      hint: 'alex@mercer.io',
                      keyboardType: TextInputType.emailAddress,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _buildTextField(
                      controller: _phoneController,
                      label: 'Phone Number',
                      hint: '+1 (555) 0199',
                      keyboardType: TextInputType.phone,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 24),

              // Section 2: Skills & Bio
              _buildSectionHeader(Icons.business_center_outlined, 'Skills & Profile Summary'),
              const SizedBox(height: 12),

              _buildTextField(
                controller: _skillsController,
                label: 'Core Skills (Comma-separated)',
                hint: 'Kotlin, Jetpack Compose, REST APIs, Room DB, Git, Python, SQL',
              ),
              const SizedBox(height: 12),

              _buildTextField(
                controller: _bioController,
                label: 'Professional Summary / Bio',
                hint: 'Enthusiastic and results-driven software professional with 3+ years experience...',
                maxLines: 4,
              ),
              const SizedBox(height: 24),

              // Section 3: Experience & Education
              _buildSectionHeader(Icons.school_outlined, 'Experience & Education'),
              const SizedBox(height: 12),

              _buildTextField(
                controller: _experienceController,
                label: 'Work Experience',
                hint: '• Senior Mobile Dev at NovaTech (2024-Present)\n• Software Eng at ByteSized Corp (2022-2024)',
                maxLines: 5,
              ),
              const SizedBox(height: 12),

              _buildTextField(
                controller: _educationController,
                label: 'Education Summary',
                hint: 'B.S. in Computer Science - University of Texas at Austin (2021)',
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSectionHeader(IconData icon, String title) {
    return Row(
      children: [
        Icon(icon, color: const Color(0xFFA78BFA), size: 20),
        const SizedBox(width: 8),
        Text(
          title,
          style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16),
        ),
      ],
    );
  }

  Widget _buildTextField({
    required TextEditingController controller,
    required String label,
    required String hint,
    int maxLines = 1,
    TextInputType keyboardType = TextInputType.text,
  }) {
    return TextField(
      controller: controller,
      maxLines: maxLines,
      keyboardType: keyboardType,
      style: const TextStyle(color: Colors.white, fontSize: 14),
      decoration: InputDecoration(
        labelText: label,
        labelStyle: const TextStyle(color: Color(0xFF64748B), fontSize: 13),
        hintText: hint,
        hintStyle: const TextStyle(color: Color(0xFF334155), fontSize: 13),
        filled: true,
        fillColor: const Color(0xFF1E293B).withOpacity(0.3),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: Color(0xFF3B82F6), width: 1.5),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: Color(0xFF334155)),
        ),
        alignLabelWithHint: true,
      ),
    );
  }
}
