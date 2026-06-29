import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:dio/dio.dart';
import '../models/models.dart';
import '../services/api_service.dart';

class JobsProvider with ChangeNotifier {
  final ApiService _apiService = ApiService();

  // Local lists
  List<JobEntity> _allJobs = [];
  UserProfileEntity _userProfile = UserProfileEntity();

  // Filters
  String _searchQuery = '';
  String _selectedCategory = 'All';
  String _selectedWorkplace = 'All';
  String _selectedJobType = 'All';
  String _selectedCountry = 'Worldwide';

  // Remote loaded states
  List<RemoteCategory> _liveCategories = [];
  List<RemoteCompany> _liveCompanies = [];
  List<JobEntity> _selectedCompanyJobs = [];
  List<RemoteLocation> _liveLocations = [];
  List<RemoteReport> _liveReports = [];
  List<RemoteTrend> _liveHomeTrends = [];
  List<RemoteReport> _liveHomeReports = [];
  List<String> _liveHomeSpotlight = [];

  RemoteJobDetail? _liveJobDetail;
  RemoteReportDetail? _liveReportDetail;

  // Loading states
  bool _isHomeDataLoading = false;
  bool _isCategoriesLoading = false;
  bool _isCompaniesLoading = false;
  bool _isCompanyJobsLoading = false;
  bool _isMarketLoading = false;
  bool _isJobDetailLoading = false;
  bool _isReportDetailLoading = false;
  bool _isLocationsLoading = false;
  bool _isReportsLoading = false;
  bool _isAiLoading = false;

  String _aiOutput = '';

  // Getters
  List<JobEntity> get allJobs => _allJobs;
  UserProfileEntity get userProfile => _userProfile;

  String get searchQuery => _searchQuery;
  String get selectedCategory => _selectedCategory;
  String get selectedWorkplace => _selectedWorkplace;
  String get selectedJobType => _selectedJobType;
  String get selectedCountry => _selectedCountry;

  List<RemoteCategory> get liveCategories => _liveCategories;
  List<RemoteCompany> get liveCompanies => _liveCompanies;
  List<JobEntity> get selectedCompanyJobs => _selectedCompanyJobs;
  List<RemoteLocation> get liveLocations => _liveLocations;
  List<RemoteReport> get liveReports => _liveReports;
  List<RemoteTrend> get liveHomeTrends => _liveHomeTrends;
  List<RemoteReport> get liveHomeReports => _liveHomeReports;
  List<String> get liveHomeSpotlight => _liveHomeSpotlight;

  RemoteJobDetail? get liveJobDetail => _liveJobDetail;
  RemoteReportDetail? get liveReportDetail => _liveReportDetail;

  bool get isHomeDataLoading => _isHomeDataLoading;
  bool get isCategoriesLoading => _isCategoriesLoading;
  bool get isCompaniesLoading => _isCompaniesLoading;
  bool get isCompanyJobsLoading => _isCompanyJobsLoading;
  bool get isMarketLoading => _isMarketLoading;
  bool get isJobDetailLoading => _isJobDetailLoading;
  bool get isReportDetailLoading => _isReportDetailLoading;
  bool get isLocationsLoading => _isLocationsLoading;
  bool get isReportsLoading => _isReportsLoading;
  bool get isAiLoading => _isAiLoading;

  String get aiOutput => _aiOutput;

  JobsProvider() {
    _loadLocalData();
  }

  // --- Local Database Helpers via SharedPreferences ---

  Future<void> _loadLocalData() async {
    try {
      final SharedPreferences prefs = await SharedPreferences.getInstance();
      
      // Load user profile
      final String? profileJson = prefs.getString('user_profile');
      if (profileJson != null) {
        _userProfile = UserProfileEntity.fromJson(jsonDecode(profileJson) as Map<String, dynamic>);
      }

      // Load all jobs
      final String? jobsJson = prefs.getString('local_jobs');
      if (jobsJson != null) {
        final List<dynamic> decoded = jsonDecode(jobsJson) as List;
        _allJobs = decoded.map((j) => JobEntity.fromJson(j as Map<String, dynamic>)).toList();
      }

      notifyListeners();
    } catch (e) {
      debugPrint('Error loading local data: $e');
    }
  }

  Future<void> _saveLocalJobs() async {
    try {
      final SharedPreferences prefs = await SharedPreferences.getInstance();
      final String jobsJson = jsonEncode(_allJobs.map((j) => j.toJson()).toList());
      await prefs.setString('local_jobs', jobsJson);
    } catch (e) {
      debugPrint('Error saving local jobs: $e');
    }
  }

  Future<void> saveUserProfile(UserProfileEntity profile) async {
    try {
      _userProfile = profile;
      final SharedPreferences prefs = await SharedPreferences.getInstance();
      await prefs.setString('user_profile', jsonEncode(profile.toJson()));
      notifyListeners();
    } catch (e) {
      debugPrint('Error saving user profile: $e');
    }
  }

  // --- Filters ---

  void setSearchQuery(String query) {
    _searchQuery = query;
    notifyListeners();
  }

  void setSelectedCategory(String category) {
    _selectedCategory = category;
    notifyListeners();
  }

  void setSelectedWorkplace(String workplace) {
    _selectedWorkplace = workplace;
    notifyListeners();
  }

  void setSelectedJobType(String jobType) {
    _selectedJobType = jobType;
    notifyListeners();
  }

  void setSelectedCountry(String country) {
    _selectedCountry = country;
    notifyListeners();
  }

  List<JobEntity> get filteredJobs {
    final list = _allJobs.where((job) {
      final matchesQuery = _searchQuery.isEmpty ||
          job.title.toLowerCase().contains(_searchQuery.toLowerCase()) ||
          job.company.toLowerCase().contains(_searchQuery.toLowerCase()) ||
          job.description.toLowerCase().contains(_searchQuery.toLowerCase());

      final matchesCategory = _selectedCategory == 'All' ||
          job.category.toLowerCase() == _selectedCategory.toLowerCase();

      final matchesWorkplace = _selectedWorkplace == 'All' ||
          job.workplace.toLowerCase() == _selectedWorkplace.toLowerCase();

      final matchesJobType = _selectedJobType == 'All' ||
          job.type.toLowerCase() == _selectedJobType.toLowerCase();

      final matchesCountry = _selectedCountry == 'Worldwide' ||
          job.location.toLowerCase().contains(_selectedCountry.toLowerCase());

      return matchesQuery && matchesCategory && matchesWorkplace && matchesJobType && matchesCountry;
    }).toList();

    list.sort((a, b) {
      if (a.active && !b.active) return -1;
      if (!a.active && b.active) return 1;
      return b.datePosted.compareTo(a.datePosted);
    });

    return list;
  }

  List<JobEntity> get bookmarkedJobs {
    return _allJobs.where((job) => job.isBookmarked).toList();
  }

  List<JobEntity> get appliedJobs {
    return _allJobs.where((job) => job.isApplied).toList();
  }

  // --- Actions ---

  Future<void> toggleBookmark(String remoteId) async {
    final int index = _allJobs.indexWhere((j) => j.remoteId == remoteId);
    if (index != -1) {
      final current = _allJobs[index];
      _allJobs[index] = current.copyWith(
        isBookmarked: !current.isBookmarked,
        status: !current.isBookmarked ? 'Saved' : 'None',
      );
      await _saveLocalJobs();
      notifyListeners();
    }
  }

  Future<void> setAppliedStatus(String remoteId, String status, String notes) async {
    final int index = _allJobs.indexWhere((j) => j.remoteId == remoteId);
    if (index != -1) {
      final current = _allJobs[index];
      _allJobs[index] = current.copyWith(
        isApplied: true,
        status: status,
        notes: notes,
      );
      await _saveLocalJobs();
      notifyListeners();
    }
  }

  // --- API Sync Helpers ---

  void _mergeRemoteJobs(List<RemoteJob> remoteJobs) {
    for (final rj in remoteJobs) {
      final int index = _allJobs.indexWhere((j) => j.remoteId == rj.id);
      if (index != -1) {
        final existing = _allJobs[index];
        _allJobs[index] = JobEntity(
          id: existing.id,
          title: rj.title,
          company: rj.company,
          logoResName: rj.logoUrl ?? rj.logoResName ?? existing.logoResName,
          location: rj.location,
          salary: rj.salary ?? existing.salary,
          type: rj.type ?? rj.employmentType ?? existing.type,
          workplace: rj.workplace ?? rj.workplaceType ?? existing.workplace,
          datePosted: rj.postedAt ?? rj.datePosted ?? existing.datePosted,
          description: rj.description ?? existing.description,
          requirements: rj.requirements ?? existing.requirements,
          benefits: rj.benefits ?? existing.benefits,
          category: rj.category ?? existing.category,
          companyWebsite: rj.companyWebsite ?? existing.companyWebsite,
          active: rj.active ?? existing.active,
          isBookmarked: existing.isBookmarked,
          isApplied: existing.isApplied,
          status: existing.status,
          notes: existing.notes,
          remoteId: rj.id ?? '',
        );
      } else {
        _allJobs.add(JobEntity.fromRemote(rj));
      }
    }
    _saveLocalJobs();
  }

  // --- API Calls ---

  Future<void> fetchHomeData(String countryName) async {
    _isHomeDataLoading = true;
    notifyListeners();

    try {
      final String countryParam = countryName == 'Worldwide' ? '' : countryName;
      final response = await _apiService.getHomeData(countryParam);
      _liveHomeTrends = response.trends ?? [];
      _liveHomeReports = response.reports ?? [];
      _liveHomeSpotlight = response.spotlightCompanies ?? [];
    } catch (e) {
      debugPrint('Error fetching home data: $e');
    } finally {
      _isHomeDataLoading = false;
      notifyListeners();
    }
  }

  Future<void> fetchCategories() async {
    _isCategoriesLoading = true;
    notifyListeners();

    try {
      _liveCategories = await _apiService.getCategories();
    } catch (e) {
      debugPrint('Error fetching categories: $e');
    } finally {
      _isCategoriesLoading = false;
      notifyListeners();
    }
  }

  Future<void> fetchLiveCompanies() async {
    _isCompaniesLoading = true;
    notifyListeners();

    try {
      _liveCompanies = await _apiService.getCompanies();
    } catch (e) {
      debugPrint('Error fetching companies: $e');
    } finally {
      _isCompaniesLoading = false;
      notifyListeners();
    }
  }

  Future<void> fetchCompanyJobs(String companyId) async {
    _isCompanyJobsLoading = true;
    notifyListeners();

    try {
      final List<RemoteJob> jobs = await _apiService.getCompanyJobs(companyId);
      _mergeRemoteJobs(jobs);

      // Filter only jobs belonging to this company locally
      _selectedCompanyJobs = _allJobs.where((j) => j.remoteId != '' && jobs.any((rj) => rj.id == j.remoteId)).toList();
    } catch (e) {
      debugPrint('Error fetching company jobs: $e');
    } finally {
      _isCompanyJobsLoading = false;
      notifyListeners();
    }
  }

  Future<void> fetchMarketData() async {
    _isMarketLoading = true;
    notifyListeners();

    try {
      final response = await _apiService.getMarketData();
      final List<RemoteJob> remoteJobs = response.activeJobs ?? response.jobs ?? [];
      _mergeRemoteJobs(remoteJobs);
    } catch (e) {
      debugPrint('Error fetching market data: $e');
    } finally {
      _isMarketLoading = false;
      notifyListeners();
    }
  }

  Future<void> fetchJobDetail(String id) async {
    _isJobDetailLoading = true;
    _liveJobDetail = null;
    notifyListeners();

    try {
      final detail = await _apiService.getJobDetail(id);
      _liveJobDetail = detail;

      // Update local db representation
      final int index = _allJobs.indexWhere((j) => j.remoteId == id);
      if (index != -1) {
        final existing = _allJobs[index];
        _allJobs[index] = existing.copyWith(
          title: detail.title,
          company: detail.company,
          logoResName: detail.logoUrl ?? detail.logoResName ?? existing.logoResName,
          location: detail.location,
          salary: detail.salary ?? existing.salary,
          type: detail.type ?? detail.employmentType ?? existing.type,
          workplace: detail.workplace ?? detail.workplaceType ?? existing.workplace,
          datePosted: detail.postedAt ?? detail.datePosted ?? existing.datePosted,
          description: detail.description ?? existing.description,
          requirements: detail.requirements ?? existing.requirements,
          benefits: detail.benefits ?? existing.benefits,
          category: detail.category ?? existing.category,
          companyWebsite: detail.companyWebsite ?? existing.companyWebsite,
          active: detail.active ?? existing.active,
        );
      } else {
        _allJobs.add(JobEntity(
          title: detail.title,
          company: detail.company,
          logoResName: detail.logoUrl ?? detail.logoResName ?? '',
          location: detail.location,
          salary: detail.salary ?? 'Tshs / Neg',
          type: detail.type ?? detail.employmentType ?? 'Full-time',
          workplace: detail.workplace ?? detail.workplaceType ?? 'Remote',
          datePosted: detail.postedAt ?? detail.datePosted ?? 'Recent',
          description: detail.description ?? '',
          requirements: detail.requirements ?? '',
          benefits: detail.benefits ?? '',
          category: detail.category ?? 'General',
          companyWebsite: detail.companyWebsite ?? 'https://jobsreport.online',
          active: detail.active ?? true,
          remoteId: detail.id ?? '',
        ));
      }
      _saveLocalJobs();

      // If there are related jobs in detail, merge them too
      if (detail.relatedJobs != null && detail.relatedJobs!.isNotEmpty) {
        _mergeRemoteJobs(detail.relatedJobs!);
      }
    } catch (e) {
      debugPrint('Error fetching job detail: $e');
    } finally {
      _isJobDetailLoading = false;
      notifyListeners();
    }
  }

  Future<void> fetchLocations() async {
    _isLocationsLoading = true;
    notifyListeners();

    try {
      _liveLocations = await _apiService.getLocations();
    } catch (e) {
      debugPrint('Error fetching locations: $e');
    } finally {
      _isLocationsLoading = false;
      notifyListeners();
    }
  }

  Future<void> fetchReports() async {
    _isReportsLoading = true;
    notifyListeners();

    try {
      _liveReports = await _apiService.getReports();
    } catch (e) {
      debugPrint('Error fetching reports: $e');
    } finally {
      _isReportsLoading = false;
      notifyListeners();
    }
  }

  Future<void> fetchReportDetail(String slug) async {
    _isReportDetailLoading = true;
    _liveReportDetail = null;
    notifyListeners();

    try {
      _liveReportDetail = await _apiService.getReportDetail(slug);
      if (_liveReportDetail?.jobs != null && _liveReportDetail!.jobs!.isNotEmpty) {
        _mergeRemoteJobs(_liveReportDetail!.jobs!);
      }
    } catch (e) {
      debugPrint('Error fetching report detail: $e');
    } finally {
      _isReportDetailLoading = false;
      notifyListeners();
    }
  }

  Future<void> fetchCategoryJobs(String categoryName) async {
    _isCategoriesLoading = true;
    notifyListeners();

    try {
      final response = await _apiService.getCategoryJobs(categoryName);
      final List<RemoteJob> remoteJobs = response.jobs ?? [];
      _mergeRemoteJobs(remoteJobs);
    } catch (e) {
      debugPrint('Error fetching category jobs: $e');
    } finally {
      _isCategoriesLoading = false;
      notifyListeners();
    }
  }

  void clearLiveJobDetail() {
    _liveJobDetail = null;
    notifyListeners();
  }

  // --- Gemini AI Career Consulting ---

  Future<void> getAiCareerAdvice(String mode, {String? optionalInput}) async {
    _isAiLoading = true;
    _aiOutput = '';
    notifyListeners();

    // Use environment variable or let it fallback
    const String apiKey = String.fromEnvironment('GEMINI_API_KEY', defaultValue: '');
    if (apiKey.isEmpty) {
      _aiOutput = 'API Key is not configured. Please enter your GEMINI_API_KEY to unlock AI features.';
      _isAiLoading = false;
      notifyListeners();
      return;
    }

    String systemPrompt = '';
    String userPrompt = '';

    if (mode == 'general') {
      systemPrompt = "You are an elite Career Advisor & HR Consultant specialized in East African and international labor markets. Give general strategic advice.";
      userPrompt = "Please analyze the modern job market and give me 5 highly actionable tips to grow my professional career. My current role info is: Name: ${_userProfile.fullName}, Target: ${_userProfile.targetTitle}, Skills: ${_userProfile.skills}, Exp: ${_userProfile.experience}, Edu: ${_userProfile.education}, Bio: ${_userProfile.bio}.";
    } else if (mode == 'resume') {
      systemPrompt = "You are an expert Resume Editor & Professional Bio polisher.";
      userPrompt = "Review my profile information and give me a polished rewrite of my professional bio, followed by 3 concrete bullet points to improve my overall resume positioning. Current Bio: ${_userProfile.bio}, Skills: ${_userProfile.skills}, Target Title: ${_userProfile.targetTitle}.";
    } else if (mode == 'interview') {
      systemPrompt = "You are a professional Interview Coach and HR Director conducting custom mock prep.";
      userPrompt = "Provide 3 standard behavioral questions for a job interview as a '${_userProfile.targetTitle}', with tips on how I should formulate my answers based on my skills: ${_userProfile.skills} and experience: ${_userProfile.experience}.";
    } else if (mode == 'custom' && optionalInput != null) {
      systemPrompt = "You are an expert career advisory assistant.";
      userPrompt = "Based on my profile (Title: ${_userProfile.targetTitle}, Skills: ${_skillsList()}, Experience: ${_userProfile.experience}), answer this user question: $optionalInput";
    }

    try {
      final Dio geminiDio = Dio();
      final response = await geminiDio.post(
        'https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey',
        data: {
          'contents': [
            {
              'parts': [
                {'text': userPrompt}
              ]
            }
          ],
          'systemInstruction': {
            'parts': [
              {'text': systemPrompt}
            ]
          },
          'generationConfig': {
            'temperature': 0.7,
            'maxOutputTokens': 1500,
          }
        },
      );

      if (response.statusCode == 200 && response.data != null) {
        final data = response.data as Map<String, dynamic>;
        final List? candidates = data['candidates'] as List?;
        final Map? firstCandidate = candidates?.firstOrNull as Map?;
        final Map? content = firstCandidate?['content'] as Map?;
        final List? parts = content?['parts'] as List?;
        final Map? firstPart = parts?.firstOrNull as Map?;
        _aiOutput = firstPart?['text'] as String? ?? 'No response generated from Gemini.';
      } else {
        _aiOutput = 'Error: Failed to fetch advice from Gemini (Code ${response.statusCode})';
      }
    } catch (e) {
      _aiOutput = 'An error occurred while communicating with Gemini: $e';
    } finally {
      _isAiLoading = false;
      notifyListeners();
    }
  }

  String _skillsList() {
    return _userProfile.skills.isEmpty ? "Not listed" : _userProfile.skills;
  }
}
extension ListGetExtension<T> on List<T> {
  T? get firstOrNull => isEmpty ? null : first;
}
