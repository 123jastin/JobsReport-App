// All data models representing API payloads and local database entities

class JobEntity {
  final int id;
  final String title;
  final String company;
  final String logoResName;
  final String location;
  final String salary;
  final String type;
  final String workplace;
  final String datePosted;
  final String description;
  final String requirements;
  final String benefits;
  final bool isBookmarked;
  final bool isApplied;
  final String status;
  final String notes;
  final String category;
  final String companyWebsite;
  final bool active;
  final String remoteId;

  JobEntity({
    this.id = 0,
    required this.title,
    required this.company,
    required this.logoResName,
    required this.location,
    required this.salary,
    required this.type,
    required this.workplace,
    required this.datePosted,
    required this.description,
    required this.requirements,
    required this.benefits,
    this.isBookmarked = false,
    this.isApplied = false,
    this.status = 'None',
    this.notes = '',
    required this.category,
    this.companyWebsite = 'https://jobsreport.online',
    this.active = true,
    this.remoteId = '',
  });

  JobEntity copyWith({
    int? id,
    String? title,
    String? company,
    String? logoResName,
    String? location,
    String? salary,
    String? type,
    String? workplace,
    String? datePosted,
    String? description,
    String? requirements,
    String? benefits,
    bool? isBookmarked,
    bool? isApplied,
    String? status,
    String? notes,
    String? category,
    String? companyWebsite,
    bool? active,
    String? remoteId,
  }) {
    return JobEntity(
      id: id ?? this.id,
      title: title ?? this.title,
      company: company ?? this.company,
      logoResName: logoResName ?? this.logoResName,
      location: location ?? this.location,
      salary: salary ?? this.salary,
      type: type ?? this.type,
      workplace: workplace ?? this.workplace,
      datePosted: datePosted ?? this.datePosted,
      description: description ?? this.description,
      requirements: requirements ?? this.requirements,
      benefits: benefits ?? this.benefits,
      isBookmarked: isBookmarked ?? this.isBookmarked,
      isApplied: isApplied ?? this.isApplied,
      status: status ?? this.status,
      notes: notes ?? this.notes,
      category: category ?? this.category,
      companyWebsite: companyWebsite ?? this.companyWebsite,
      active: active ?? this.active,
      remoteId: remoteId ?? this.remoteId,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'company': company,
      'logoResName': logoResName,
      'location': location,
      'salary': salary,
      'type': type,
      'workplace': workplace,
      'datePosted': datePosted,
      'description': description,
      'requirements': requirements,
      'benefits': benefits,
      'isBookmarked': isBookmarked ? 1 : 0,
      'isApplied': isApplied ? 1 : 0,
      'status': status,
      'notes': notes,
      'category': category,
      'companyWebsite': companyWebsite,
      'active': active ? 1 : 0,
      'remoteId': remoteId,
    };
  }

  factory JobEntity.fromJson(Map<String, dynamic> json) {
    return JobEntity(
      id: json['id'] as int? ?? 0,
      title: json['title'] as String? ?? '',
      company: json['company'] as String? ?? '',
      logoResName: json['logoResName'] as String? ?? '',
      location: json['location'] as String? ?? '',
      salary: json['salary'] as String? ?? '',
      type: json['type'] as String? ?? '',
      workplace: json['workplace'] as String? ?? '',
      datePosted: json['datePosted'] as String? ?? '',
      description: json['description'] as String? ?? '',
      requirements: json['requirements'] as String? ?? '',
      benefits: json['benefits'] as String? ?? '',
      isBookmarked: (json['isBookmarked'] as int? ?? 0) == 1,
      isApplied: (json['isApplied'] as int? ?? 0) == 1,
      status: json['status'] as String? ?? 'None',
      notes: json['notes'] as String? ?? '',
      category: json['category'] as String? ?? '',
      companyWebsite: json['companyWebsite'] as String? ?? 'https://jobsreport.online',
      active: (json['active'] as int? ?? 1) == 1,
      remoteId: json['remoteId'] as String? ?? '',
    );
  }

  factory JobEntity.fromRemote(RemoteJob remote, {int localId = 0}) {
    return JobEntity(
      id: localId,
      title: remote.title,
      company: remote.company,
      logoResName: remote.logoUrl ?? remote.logoResName ?? '',
      location: remote.location,
      salary: remote.salary ?? 'Tshs / Neg',
      type: remote.type ?? remote.employmentType ?? 'Full-time',
      workplace: remote.workplace ?? remote.workplaceType ?? 'Remote',
      datePosted: remote.postedAt ?? remote.datePosted ?? 'Recent',
      description: remote.description ?? '',
      requirements: remote.requirements ?? '',
      benefits: remote.benefits ?? '',
      category: remote.category ?? 'General',
      companyWebsite: remote.companyWebsite ?? 'https://jobsreport.online',
      active: remote.active ?? true,
      remoteId: remote.id ?? '',
    );
  }
}

class UserProfileEntity {
  final int id;
  final String fullName;
  final String email;
  final String phone;
  final String targetTitle;
  final String skills;
  final String experience;
  final String education;
  final String bio;

  UserProfileEntity({
    this.id = 1,
    this.fullName = '',
    this.email = '',
    this.phone = '',
    this.targetTitle = '',
    this.skills = '',
    this.experience = '',
    this.education = '',
    this.bio = '',
  });

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'fullName': fullName,
      'email': email,
      'phone': phone,
      'targetTitle': targetTitle,
      'skills': skills,
      'experience': experience,
      'education': education,
      'bio': bio,
    };
  }

  factory UserProfileEntity.fromJson(Map<String, dynamic> json) {
    return UserProfileEntity(
      id: json['id'] as int? ?? 1,
      fullName: json['fullName'] as String? ?? '',
      email: json['email'] as String? ?? '',
      phone: json['phone'] as String? ?? '',
      targetTitle: json['targetTitle'] as String? ?? '',
      skills: json['skills'] as String? ?? '',
      experience: json['experience'] as String? ?? '',
      education: json['education'] as String? ?? '',
      bio: json['bio'] as String? ?? '',
    );
  }
}

class RemoteJobImage {
  final String url;
  final String? thumbnail;
  final String? name;
  final String? type;
  final String? seoTitle;
  final String? seoDescription;
  final String? caption;

  RemoteJobImage({
    required this.url,
    this.thumbnail,
    this.name,
    this.type,
    this.seoTitle,
    this.seoDescription,
    this.caption,
  });

  factory RemoteJobImage.fromJson(Map<String, dynamic> json) {
    return RemoteJobImage(
      url: json['url'] as String? ?? '',
      thumbnail: json['thumbnail'] as String?,
      name: json['name'] as String?,
      type: json['type'] as String?,
      seoTitle: json['seoTitle'] as String?,
      seoDescription: json['seoDescription'] as String?,
      caption: json['caption'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'url': url,
      'thumbnail': thumbnail,
      'name': name,
      'type': type,
      'seoTitle': seoTitle,
      'seoDescription': seoDescription,
      'caption': caption,
    };
  }
}

class RemoteJob {
  final String? id;
  final String title;
  final String company;
  final String? logoResName;
  final String location;
  final String? salary;
  final String? type;
  final String? workplace;
  final String? datePosted;
  final String? description;
  final String? requirements;
  final String? benefits;
  final String? category;
  final String? companyWebsite;
  final bool? active;
  final String? postedAt;
  final String? expiresAt;
  final String? url;
  final String? logoUrl;
  final String? whatsappNumber;
  final String? applicationInstructions;
  final String? salaryCurrencyFlag;
  final String? employmentType;
  final String? workplaceType;
  final String? educationLevel;
  final int? experienceMonths;
  final List<RemoteJobImage>? images;

  RemoteJob({
    this.id,
    required this.title,
    required this.company,
    this.logoResName,
    required this.location,
    this.salary,
    this.type,
    this.workplace,
    this.datePosted,
    this.description,
    this.requirements,
    this.benefits,
    this.category,
    this.companyWebsite,
    this.active,
    this.postedAt,
    this.expiresAt,
    this.url,
    this.logoUrl,
    this.whatsappNumber,
    this.applicationInstructions,
    this.salaryCurrencyFlag,
    this.employmentType,
    this.workplaceType,
    this.educationLevel,
    this.experienceMonths,
    this.images,
  });

  factory RemoteJob.fromJson(Map<String, dynamic> json) {
    var list = json['images'] as List?;
    List<RemoteJobImage>? imagesList = list != null
        ? list.map((i) => RemoteJobImage.fromJson(i as Map<String, dynamic>)).toList()
        : null;

    return RemoteJob(
      id: json['id'] as String?,
      title: json['title'] as String? ?? '',
      company: json['company'] as String? ?? '',
      logoResName: json['logoResName'] as String?,
      location: json['location'] as String? ?? '',
      salary: json['salary'] as String?,
      type: json['type'] as String?,
      workplace: json['workplace'] as String?,
      datePosted: json['datePosted'] as String?,
      description: json['description'] as String?,
      requirements: json['requirements'] as String?,
      benefits: json['benefits'] as String?,
      category: json['category'] as String?,
      companyWebsite: json['companyWebsite'] as String?,
      active: json['active'] as bool?,
      postedAt: json['postedAt'] as String?,
      expiresAt: json['expiresAt'] as String?,
      url: json['url'] as String?,
      logoUrl: json['logoUrl'] as String?,
      whatsappNumber: json['whatsapp_number'] as String?,
      applicationInstructions: json['application_instructions'] as String?,
      salaryCurrencyFlag: json['salary_currency_flag'] as String?,
      employmentType: json['employment_type'] as String?,
      workplaceType: json['workplace_type'] as String?,
      educationLevel: json['education_level'] as String?,
      experienceMonths: json['experience_months'] as int?,
      images: imagesList,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'company': company,
      'logoResName': logoResName,
      'location': location,
      'salary': salary,
      'type': type,
      'workplace': workplace,
      'datePosted': datePosted,
      'description': description,
      'requirements': requirements,
      'benefits': benefits,
      'category': category,
      'companyWebsite': companyWebsite,
      'active': active,
      'postedAt': postedAt,
      'expiresAt': expiresAt,
      'url': url,
      'logoUrl': logoUrl,
      'whatsapp_number': whatsappNumber,
      'application_instructions': applicationInstructions,
      'salary_currency_flag': salaryCurrencyFlag,
      'employment_type': employmentType,
      'workplace_type': workplaceType,
      'education_level': educationLevel,
      'experience_months': experienceMonths,
      'images': images?.map((i) => i.toJson()).toList(),
    };
  }
}

class RemoteJobDetail {
  final String? id;
  final String title;
  final String company;
  final String? logoResName;
  final String location;
  final String? salary;
  final String? type;
  final String? workplace;
  final String? datePosted;
  final String? description;
  final String? requirements;
  final String? benefits;
  final String? category;
  final String? companyWebsite;
  final bool? active;
  final List<RemoteJob>? relatedJobs;
  final String? postedAt;
  final String? expiresAt;
  final String? url;
  final String? logoUrl;
  final String? whatsappNumber;
  final String? applicationInstructions;
  final String? salaryCurrencyFlag;
  final String? employmentType;
  final String? workplaceType;
  final String? educationLevel;
  final int? experienceMonths;
  final List<RemoteJobImage>? images;

  RemoteJobDetail({
    this.id,
    required this.title,
    required this.company,
    this.logoResName,
    required this.location,
    this.salary,
    this.type,
    this.workplace,
    this.datePosted,
    this.description,
    this.requirements,
    this.benefits,
    this.category,
    this.companyWebsite,
    this.active,
    this.relatedJobs,
    this.postedAt,
    this.expiresAt,
    this.url,
    this.logoUrl,
    this.whatsappNumber,
    this.applicationInstructions,
    this.salaryCurrencyFlag,
    this.employmentType,
    this.workplaceType,
    this.educationLevel,
    this.experienceMonths,
    this.images,
  });

  factory RemoteJobDetail.fromJson(Map<String, dynamic> json) {
    var relatedList = json['relatedJobs'] as List?;
    List<RemoteJob>? relatedJobsList = relatedList != null
        ? relatedList.map((i) => RemoteJob.fromJson(i as Map<String, dynamic>)).toList()
        : null;

    var list = json['images'] as List?;
    List<RemoteJobImage>? imagesList = list != null
        ? list.map((i) => RemoteJobImage.fromJson(i as Map<String, dynamic>)).toList()
        : null;

    return RemoteJobDetail(
      id: json['id'] as String?,
      title: json['title'] as String? ?? '',
      company: json['company'] as String? ?? '',
      logoResName: json['logoResName'] as String?,
      location: json['location'] as String? ?? '',
      salary: json['salary'] as String?,
      type: json['type'] as String?,
      workplace: json['workplace'] as String?,
      datePosted: json['datePosted'] as String?,
      description: json['description'] as String?,
      requirements: json['requirements'] as String?,
      benefits: json['benefits'] as String?,
      category: json['category'] as String?,
      companyWebsite: json['companyWebsite'] as String?,
      active: json['active'] as bool?,
      relatedJobs: relatedJobsList,
      postedAt: json['postedAt'] as String?,
      expiresAt: json['expiresAt'] as String?,
      url: json['url'] as String?,
      logoUrl: json['logoUrl'] as String?,
      whatsappNumber: json['whatsapp_number'] as String?,
      applicationInstructions: json['application_instructions'] as String?,
      salaryCurrencyFlag: json['salary_currency_flag'] as String?,
      employmentType: json['employment_type'] as String?,
      workplaceType: json['workplace_type'] as String?,
      educationLevel: json['education_level'] as String?,
      experienceMonths: json['experience_months'] as int?,
      images: imagesList,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'company': company,
      'logoResName': logoResName,
      'location': location,
      'salary': salary,
      'type': type,
      'workplace': workplace,
      'datePosted': datePosted,
      'description': description,
      'requirements': requirements,
      'benefits': benefits,
      'category': category,
      'companyWebsite': companyWebsite,
      'active': active,
      'relatedJobs': relatedJobs?.map((r) => r.toJson()).toList(),
      'postedAt': postedAt,
      'expiresAt': expiresAt,
      'url': url,
      'logoUrl': logoUrl,
      'whatsapp_number': whatsappNumber,
      'application_instructions': applicationInstructions,
      'salary_currency_flag': salaryCurrencyFlag,
      'employment_type': employmentType,
      'workplace_type': workplaceType,
      'education_level': educationLevel,
      'experience_months': experienceMonths,
      'images': images?.map((i) => i.toJson()).toList(),
    };
  }
}

class MarketStats {
  final int? totalJobs;
  final int? activeJobs;

  MarketStats({this.totalJobs, this.activeJobs});

  factory MarketStats.fromJson(Map<String, dynamic> json) {
    return MarketStats(
      totalJobs: json['totalJobs'] as int?,
      activeJobs: json['activeJobs'] as int?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'totalJobs': totalJobs,
      'activeJobs': activeJobs,
    };
  }
}

class MarketResponse {
  final List<RemoteJob>? activeJobs;
  final List<RemoteJob>? jobs;
  final List<RemoteCompany>? companies;
  final List<String>? roles;
  final MarketStats? stats;

  MarketResponse({
    this.activeJobs,
    this.jobs,
    this.companies,
    this.roles,
    this.stats,
  });

  factory MarketResponse.fromJson(Map<String, dynamic> json) {
    var activeList = json['activeJobs'] as List?;
    var jobsList = json['jobs'] as List?;
    var companiesList = json['companies'] as List?;
    var rolesList = json['roles'] as List?;

    return MarketResponse(
      activeJobs: activeList?.map((i) => RemoteJob.fromJson(i)).toList(),
      jobs: jobsList?.map((i) => RemoteJob.fromJson(i)).toList(),
      companies: companiesList?.map((i) => RemoteCompany.fromJson(i)).toList(),
      roles: rolesList?.map((i) => i as String).toList(),
      stats: json['stats'] != null ? MarketStats.fromJson(json['stats']) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'activeJobs': activeJobs?.map((i) => i.toJson()).toList(),
      'jobs': jobs?.map((i) => i.toJson()).toList(),
      'companies': companies?.map((i) => i.toJson()).toList(),
      'roles': roles,
      'stats': stats?.toJson(),
    };
  }
}

class CategoryJobsResponse {
  final List<RemoteJob>? jobs;

  CategoryJobsResponse({this.jobs});

  factory CategoryJobsResponse.fromJson(Map<String, dynamic> json) {
    var list = json['jobs'] as List?;
    return CategoryJobsResponse(
      jobs: list?.map((i) => RemoteJob.fromJson(i)).toList(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'jobs': jobs?.map((i) => i.toJson()).toList(),
    };
  }
}

class RemoteCategory {
  final String name;
  final String? slug;
  final String? id;
  final int? jobCount;
  final int? jobsCount;
  final int? count;

  RemoteCategory({
    required this.name,
    this.slug,
    this.id,
    this.jobCount,
    this.jobsCount,
    this.count,
  });

  factory RemoteCategory.fromJson(Map<String, dynamic> json) {
    return RemoteCategory(
      name: json['name'] as String? ?? '',
      slug: json['slug'] as String?,
      id: json['id'] as String?,
      jobCount: json['jobCount'] as int? ?? json['jobsCount'] as int? ?? json['count'] as int?,
      jobsCount: json['jobsCount'] as int?,
      count: json['count'] as int?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'slug': slug,
      'id': id,
      'jobCount': jobCount,
      'jobsCount': jobsCount,
      'count': count,
    };
  }
}

class RemoteMetric {
  final String label;
  final double value;

  RemoteMetric({required this.label, required this.value});

  factory RemoteMetric.fromJson(Map<String, dynamic> json) {
    return RemoteMetric(
      label: json['label'] as String? ?? '',
      value: (json['value'] as num? ?? 0.0).toDouble(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'label': label,
      'value': value,
    };
  }
}

class RemoteReport {
  final String? id;
  final String? slug;
  final String title;
  final String? author;
  final String? date;
  final String? monthYear;
  final String? category;
  final String? excerpt;
  final String? summary;
  final String? country;
  final String? role;
  final String? updatedAt;
  final List<RemoteMetric>? metrics;

  RemoteReport({
    this.id,
    this.slug,
    this.title,
    this.author,
    this.date,
    this.monthYear,
    this.category,
    this.excerpt,
    this.summary,
    this.country,
    this.role,
    this.updatedAt,
    this.metrics,
  });

  factory RemoteReport.fromJson(Map<String, dynamic> json) {
    var metricsList = json['metrics'] as List?;
    return RemoteReport(
      id: json['id'] as String?,
      slug: json['slug'] as String?,
      title: json['title'] as String? ?? '',
      author: json['author'] as String?,
      date: json['date'] as String?,
      monthYear: json['monthYear'] as String?,
      category: json['category'] as String?,
      excerpt: json['excerpt'] as String?,
      summary: json['summary'] as String?,
      country: json['country'] as String?,
      role: json['role'] as String?,
      updatedAt: json['updatedAt'] as String?,
      metrics: metricsList?.map((m) => RemoteMetric.fromJson(m)).toList(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'slug': slug,
      'title': title,
      'author': author,
      'date': date,
      'monthYear': monthYear,
      'category': category,
      'excerpt': excerpt,
      'summary': summary,
      'country': country,
      'role': role,
      'updatedAt': updatedAt,
      'metrics': metrics?.map((m) => m.toJson()).toList(),
    };
  }
}

class RemoteReportStats {
  final int? companies;
  final int? growth;

  RemoteReportStats({this.companies, this.growth});

  factory RemoteReportStats.fromJson(Map<String, dynamic> json) {
    return RemoteReportStats(
      companies: json['companies'] as int?,
      growth: json['growth'] as int?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'companies': companies,
      'growth': growth,
    };
  }
}

class RemoteChartDataItem {
  final String name;
  final double demand;

  RemoteChartDataItem({required this.name, required this.demand});

  factory RemoteChartDataItem.fromJson(Map<String, dynamic> json) {
    return RemoteChartDataItem(
      name: json['name'] as String? ?? '',
      demand: (json['demand'] as num? ?? 0.0).toDouble(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'demand': demand,
    };
  }
}

class RemoteDistributionItem {
  final String name;
  final double value;

  RemoteDistributionItem({required this.name, required this.value});

  factory RemoteDistributionItem.fromJson(Map<String, dynamic> json) {
    return RemoteDistributionItem(
      name: json['name'] as String? ?? '',
      value: (json['value'] as num? ?? 0.0).toDouble(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'value': value,
    };
  }
}

class RemoteCompanyItem {
  final String name;
  final String? url;

  RemoteCompanyItem({required this.name, this.url});

  factory RemoteCompanyItem.fromJson(Map<String, dynamic> json) {
    return RemoteCompanyItem(
      name: json['name'] as String? ?? '',
      url: json['url'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'url': url,
    };
  }
}

class RemoteReportDetail {
  final String? id;
  final String? slug;
  final String title;
  final String? author;
  final String? date;
  final String? monthYear;
  final String? category;
  final String? excerpt;
  final String? content;
  final String? summary;
  final String? country;
  final String? role;
  final String? updatedAt;
  final String? createdAt;
  final List<RemoteMetric>? metrics;
  final RemoteReportStats? stats;
  final List<RemoteChartDataItem>? chartData;
  final List<RemoteDistributionItem>? distribution;
  final List<RemoteCompanyItem>? companies;
  final List<RemoteJob>? jobs;

  RemoteReportDetail({
    this.id,
    this.slug,
    required this.title,
    this.author,
    this.date,
    this.monthYear,
    this.category,
    this.excerpt,
    this.content,
    this.summary,
    this.country,
    this.role,
    this.updatedAt,
    this.createdAt,
    this.metrics,
    this.stats,
    this.chartData,
    this.distribution,
    this.companies,
    this.jobs,
  });

  factory RemoteReportDetail.fromJson(Map<String, dynamic> json) {
    return RemoteReportDetail(
      id: json['id'] as String?,
      slug: json['slug'] as String?,
      title: json['title'] as String? ?? '',
      author: json['author'] as String?,
      date: json['date'] as String?,
      monthYear: json['monthYear'] as String?,
      category: json['category'] as String?,
      excerpt: json['excerpt'] as String?,
      content: json['content'] as String?,
      summary: json['summary'] as String?,
      country: json['country'] as String?,
      role: json['role'] as String?,
      updatedAt: json['updatedAt'] as String?,
      createdAt: json['createdAt'] as String?,
      metrics: (json['metrics'] as List?)?.map((m) => RemoteMetric.fromJson(m)).toList(),
      stats: json['stats'] != null ? RemoteReportStats.fromJson(json['stats']) : null,
      chartData: (json['chartData'] as List?)?.map((c) => RemoteChartDataItem.fromJson(c)).toList(),
      distribution: (json['distribution'] as List?)?.map((d) => RemoteDistributionItem.fromJson(d)).toList(),
      companies: (json['companies'] as List?)?.map((c) => RemoteCompanyItem.fromJson(c)).toList(),
      jobs: (json['jobs'] as List?)?.map((j) => RemoteJob.fromJson(j)).toList(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'slug': slug,
      'title': title,
      'author': author,
      'date': date,
      'monthYear': monthYear,
      'category': category,
      'excerpt': excerpt,
      'content': content,
      'summary': summary,
      'country': country,
      'role': role,
      'updatedAt': updatedAt,
      'createdAt': createdAt,
      'metrics': metrics?.map((m) => m.toJson()).toList(),
      'stats': stats?.toJson(),
      'chartData': chartData?.map((c) => c.toJson()).toList(),
      'distribution': distribution?.map((d) => d.toJson()).toList(),
      'companies': companies?.map((c) => c.toJson()).toList(),
      'jobs': jobs?.map((j) => j.toJson()).toList(),
    };
  }
}

class RemoteTrend {
  final String? phrase;
  final String? volume;
  final String? trend;
  final String? name;
  final String? growth;

  RemoteTrend({this.phrase, this.volume, this.trend, this.name, this.growth});

  factory RemoteTrend.fromJson(Map<String, dynamic> json) {
    return RemoteTrend(
      phrase: json['phrase'] as String?,
      volume: json['volume'] as String?,
      trend: json['trend'] as String?,
      name: json['name'] as String?,
      growth: json['growth'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'phrase': phrase,
      'volume': volume,
      'trend': trend,
      'name': name,
      'growth': growth,
    };
  }
}

class HomeDataResponse {
  final List<RemoteTrend>? trends;
  final List<RemoteReport>? reports;
  final List<String>? spotlightCompanies;

  HomeDataResponse({this.trends, this.reports, this.spotlightCompanies});

  factory HomeDataResponse.fromJson(Map<String, dynamic> json) {
    return HomeDataResponse(
      trends: (json['trends'] as List?)?.map((t) => RemoteTrend.fromJson(t)).toList(),
      reports: (json['reports'] as List?)?.map((r) => RemoteReport.fromJson(r)).toList(),
      spotlightCompanies: (json['spotlightCompanies'] as List?)?.map((s) => s as String).toList(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'trends': trends?.map((t) => t.toJson()).toList(),
      'reports': reports?.map((r) => r.toJson()).toList(),
      'spotlightCompanies': spotlightCompanies,
    };
  }
}

class RemoteLocation {
  final String name;
  final String country;
  final String? region;
  final String? postcode;

  RemoteLocation({required this.name, required this.country, this.region, this.postcode});

  factory RemoteLocation.fromJson(Map<String, dynamic> json) {
    return RemoteLocation(
      name: json['name'] as String? ?? '',
      country: json['country'] as String? ?? '',
      region: json['region'] as String?,
      postcode: json['postcode'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'country': country,
      'region': region,
      'postcode': postcode,
    };
  }
}

class RemoteCompany {
  final String? id;
  final String name;
  final String? website;
  final String? url;
  final String? description;
  final String? industry;
  final String? foundedYear;
  final String? employeeCount;
  final String? streetAddress;
  final String? area;
  final String? locality;
  final String? district;
  final String? postalCode;
  final String? postalArea;
  final String? country;
  final String? logoUrl;
  final String? logoResName;
  final int? totalJobs;
  final int? activeJobs;

  RemoteCompany({
    this.id,
    required this.name,
    this.website,
    this.url,
    this.description,
    this.industry,
    this.foundedYear,
    this.employeeCount,
    this.streetAddress,
    this.area,
    this.locality,
    this.district,
    this.postalCode,
    this.postalArea,
    this.country,
    this.logoUrl,
    this.logoResName,
    this.totalJobs,
    this.activeJobs,
  });

  factory RemoteCompany.fromJson(Map<String, dynamic> json) {
    return RemoteCompany(
      id: json['id'] as String?,
      name: json['name'] as String? ?? '',
      website: json['website'] as String?,
      url: json['url'] as String?,
      description: json['description'] as String?,
      industry: json['industry'] as String?,
      foundedYear: json['foundedYear'] as String?,
      employeeCount: json['employeeCount'] as String?,
      streetAddress: json['streetAddress'] as String?,
      area: json['area'] as String?,
      locality: json['locality'] as String?,
      district: json['district'] as String?,
      postalCode: json['postalCode'] as String?,
      postalArea: json['postalArea'] as String?,
      country: json['country'] as String?,
      logoUrl: json['logoUrl'] as String?,
      logoResName: json['logoResName'] as String?,
      totalJobs: json['totalJobs'] as int?,
      activeJobs: json['activeJobs'] as int?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'website': website,
      'url': url,
      'description': description,
      'industry': industry,
      'foundedYear': foundedYear,
      'employeeCount': employeeCount,
      'streetAddress': streetAddress,
      'area': area,
      'locality': locality,
      'district': district,
      'postalCode': postalCode,
      'postalArea': postalArea,
      'country': country,
      'logoUrl': logoUrl,
      'logoResName': logoResName,
      'totalJobs': totalJobs,
      'activeJobs': activeJobs,
    };
  }
}
