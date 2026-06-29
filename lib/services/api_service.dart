import 'package:dio/dio.dart';
import '../models/models.dart';

class ApiService {
  final Dio _dio;

  ApiService()
      : _dio = Dio(
          BaseOptions(
            baseUrl: 'https://jobsreport.online/',
            connectTimeout: const Duration(seconds: 15),
            receiveTimeout: const Duration(seconds: 15),
            headers: {
              'Content-Type': 'application/json',
              'Accept': 'application/json',
            },
          ),
        );

  // 1. /api/market
  Future<MarketResponse> getMarketData({int limit = 100, int page = 1}) async {
    try {
      final response = await _dio.get(
        'api/market',
        queryParameters: {
          'limit': limit,
          'page': page,
        },
      );
      if (response.data != null) {
        return MarketResponse.fromJson(response.data as Map<String, dynamic>);
      }
      throw Exception('Failed to load market data: empty body');
    } catch (e) {
      throw Exception('Failed to load market data: $e');
    }
  }

  // 2. /api/job-detail/{id}
  Future<RemoteJobDetail> getJobDetail(String id) async {
    try {
      final response = await _dio.get('api/job-detail/$id');
      if (response.data != null) {
        return RemoteJobDetail.fromJson(response.data as Map<String, dynamic>);
      }
      throw Exception('Failed to load job detail: empty body');
    } catch (e) {
      throw Exception('Failed to load job detail: $e');
    }
  }

  // 3. /api/categories
  Future<List<RemoteCategory>> getCategories() async {
    try {
      final response = await _dio.get('api/categories');
      if (response.data != null) {
        final list = response.data as List;
        return list.map((item) => RemoteCategory.fromJson(item as Map<String, dynamic>)).toList();
      }
      return [];
    } catch (e) {
      throw Exception('Failed to load categories: $e');
    }
  }

  // 4. /api/companies-jobs
  Future<List<RemoteCompany>> getCompanies() async {
    try {
      final response = await _dio.get('api/companies-jobs');
      if (response.data != null) {
        final list = response.data as List;
        return list.map((item) => RemoteCompany.fromJson(item as Map<String, dynamic>)).toList();
      }
      return [];
    } catch (e) {
      throw Exception('Failed to load companies: $e');
    }
  }

  // 5. /api/company-jobs/{id}
  Future<List<RemoteJob>> getCompanyJobs(String companyId) async {
    try {
      final response = await _dio.get('api/company-jobs/$companyId');
      if (response.data != null) {
        final list = response.data as List;
        return list.map((item) => RemoteJob.fromJson(item as Map<String, dynamic>)).toList();
      }
      return [];
    } catch (e) {
      throw Exception('Failed to load company jobs: $e');
    }
  }

  // 6. /api/category-jobs
  Future<CategoryJobsResponse> getCategoryJobs(String category) async {
    try {
      final response = await _dio.get(
        'api/category-jobs',
        queryParameters: {'category': category},
      );
      if (response.data != null) {
        return CategoryJobsResponse.fromJson(response.data as Map<String, dynamic>);
      }
      throw Exception('Failed to load category jobs: empty body');
    } catch (e) {
      throw Exception('Failed to load category jobs: $e');
    }
  }

  // 7. /api/home
  Future<HomeDataResponse> getHomeData(String country) async {
    try {
      final response = await _dio.get(
        'api/home',
        queryParameters: {'country': country},
      );
      if (response.data != null) {
        return HomeDataResponse.fromJson(response.data as Map<String, dynamic>);
      }
      throw Exception('Failed to load home data: empty body');
    } catch (e) {
      throw Exception('Failed to load home data: $e');
    }
  }

  // 8. /api/locations
  Future<List<RemoteLocation>> getLocations() async {
    try {
      final response = await _dio.get('api/locations');
      if (response.data != null) {
        final list = response.data as List;
        return list.map((item) => RemoteLocation.fromJson(item as Map<String, dynamic>)).toList();
      }
      return [];
    } catch (e) {
      throw Exception('Failed to load locations: $e');
    }
  }

  // 9. /api/reports
  Future<List<RemoteReport>> getReports() async {
    try {
      final response = await _dio.get('api/reports');
      if (response.data != null) {
        final list = response.data as List;
        return list.map((item) => RemoteReport.fromJson(item as Map<String, dynamic>)).toList();
      }
      return [];
    } catch (e) {
      throw Exception('Failed to load reports: $e');
    }
  }

  // 10. /api/reports/{slug}
  Future<RemoteReportDetail> getReportDetail(String slug) async {
    try {
      final response = await _dio.get('api/reports/$slug');
      if (response.data != null) {
        return RemoteReportDetail.fromJson(response.data as Map<String, dynamic>);
      }
      throw Exception('Failed to load report detail: empty body');
    } catch (e) {
      throw Exception('Failed to load report detail: $e');
    }
  }
}
