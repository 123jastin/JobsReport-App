import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/jobs_provider.dart';
import 'screens/splash_screen.dart';
import 'screens/home_screen.dart';
import 'screens/job_detail_screen.dart';
import 'screens/companies_screen.dart';
import 'screens/market_screen.dart';
import 'screens/category_screen.dart';
import 'screens/saved_jobs_screen.dart';
import 'screens/ai_career_screen.dart';
import 'screens/profile_screen.dart';
import 'screens/about_us_screen.dart';
import 'screens/contact_us_screen.dart';
import 'screens/disclaimer_screen.dart';
import 'screens/privacy_policy_screen.dart';
import 'screens/terms_of_service_screen.dart';
import 'screens/intelligence_feed_screen.dart';
import 'screens/job_reports_screen.dart';
import 'screens/report_detail_screen.dart';
import 'screens/regions_screen.dart';
import 'screens/region_detail_screen.dart';
import 'screens/admin_studio_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => JobsProvider()),
      ],
      child: const MyApp(),
    ),
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Jobs Report & Career Guide',
      debugShowCheckedModeBanner: false,
      themeMode: ThemeMode.dark,
      darkTheme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF0F172A), // Slate-900
        primaryColor: const Color(0xFF3B82F6), // Vibrant Blue
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFF3B82F6),
          secondary: Color(0xFFF59E0B), // Amber-500
          surface: Color(0xFF1E293B), // Slate-800
          background: Color(0xFF0F172A), // Slate-900
          onPrimary: Colors.white,
          onSecondary: Colors.black,
          onSurface: Colors.white,
          onBackground: Color(0xFF94A3B8), // Slate-400
        ),
        cardTheme: CardTheme(
          color: const Color(0xFF1E293B),
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
            side: BorderSide(
              color: const Color(0xFF334155).withOpacity(0.3),
              width: 1,
            ),
          ),
        ),
        appBarTheme: const AppBarTheme(
          backgroundColor: Color(0xFF0F172A),
          elevation: 0,
          centerTitle: false,
          titleTextStyle: TextStyle(
            color: Colors.white,
            fontSize: 20,
            fontWeight: FontWeight.bold,
          ),
          iconTheme: IconThemeData(color: Colors.white),
        ),
        textTheme: const TextTheme(
          displayLarge: TextStyle(fontFamily: 'Space Grotesk', fontWeight: FontWeight.bold, color: Colors.white),
          headlineLarge: TextStyle(fontFamily: 'Space Grotesk', fontWeight: FontWeight.bold, color: Colors.white),
          titleLarge: TextStyle(fontWeight: FontWeight.bold, color: Colors.white),
          bodyLarge: TextStyle(color: Color(0xFFE2E8F0)),
          bodyMedium: TextStyle(color: Color(0xFF94A3B8)),
        ),
      ),
      initialRoute: '/',
      onGenerateRoute: (settings) {
        final Uri uri = Uri.parse(settings.name ?? '');
        final List<String> pathSegments = uri.pathSegments;

        // Dynamic Parameter Routes
        if (pathSegments.length == 2) {
          final String prefix = pathSegments[0];
          final String param = pathSegments[1];

          if (prefix == 'job_detail') {
            final int? id = int.tryParse(param);
            if (id != null) {
              return MaterialPageRoute(
                builder: (context) => JobDetailScreen(jobId: id),
                settings: settings,
              );
            }
          }

          if (prefix == 'country') {
            return MaterialPageRoute(
              builder: (context) => HomeScreen(countrySlug: param),
              settings: settings,
            );
          }

          if (prefix == 'category') {
            return MaterialPageRoute(
              builder: (context) => CategoryScreen(categorySlug: param, countrySlug: null),
              settings: settings,
            );
          }

          if (prefix == 'region_detail') {
            return MaterialPageRoute(
              builder: (context) => RegionDetailScreen(regionName: Uri.decodeComponent(param)),
              settings: settings,
            );
          }

          if (prefix == 'report_detail') {
            return MaterialPageRoute(
              builder: (context) => ReportDetailScreen(
                slug: param,
                onNavigateToJobDetail: (id) {
                  Navigator.pushNamed(context, 'job_detail/$id');
                },
              ),
              settings: settings,
            );
          }
        }

        // Three Segments: category/:categorySlug/:countrySlug
        if (pathSegments.length == 3 && pathSegments[0] == 'category') {
          return MaterialPageRoute(
            builder: (context) => CategoryScreen(
              categorySlug: pathSegments[1],
              countrySlug: pathSegments[2],
            ),
            settings: settings,
          );
        }

        // Static Routes
        switch (settings.name) {
          case '/':
            return MaterialPageRoute(builder: (context) => const SplashScreen());
          case '/home':
            return MaterialPageRoute(builder: (context) => const HomeScreen());
          case '/dashboard':
          case '/market':
            return MaterialPageRoute(builder: (context) => const MarketScreen());
          case '/companies':
            return MaterialPageRoute(builder: (context) => const CompaniesScreen());
          case '/regions':
            return MaterialPageRoute(builder: (context) => const RegionsScreen());
          case '/about_us':
            return MaterialPageRoute(builder: (context) => const AboutUsScreen());
          case '/contact_us':
            return MaterialPageRoute(builder: (context) => const ContactUsScreen());
          case '/disclaimer':
            return MaterialPageRoute(builder: (context) => const DisclaimerScreen());
          case '/privacy_policy':
            return MaterialPageRoute(builder: (context) => const PrivacyPolicyScreen());
          case '/terms_of_service':
            return MaterialPageRoute(builder: (context) => const TermsOfServiceScreen());
          case '/intelligence_feed':
            return MaterialPageRoute(
              builder: (context) => IntelligenceFeedScreen(
                onNavigateToJobs: () {
                  Navigator.pushReplacementNamed(context, '/market');
                },
              ),
            );
          case '/reports':
            return MaterialPageRoute(
              builder: (context) => JobReportsScreen(
                onNavigateToReportDetail: (slug) {
                  Navigator.pushNamed(context, 'report_detail/$slug');
                },
              ),
            );
          case '/admin_studio':
            return MaterialPageRoute(builder: (context) => const AdminStudioScreen());
          case '/tracker':
          case '/saved_jobs':
            return MaterialPageRoute(builder: (context) => const SavedJobsScreen());
          case '/ai_advisor':
          case '/ai_career':
            return MaterialPageRoute(builder: (context) => const AiCareerScreen());
          case '/profile':
            return MaterialPageRoute(builder: (context) => const ProfileScreen());
          default:
            return MaterialPageRoute(
              builder: (context) => Scaffold(
                appBar: AppBar(title: const Text('Not Found')),
                body: const Center(child: Text('Page not found')),
              ),
            );
        }
      },
    );
  }
}
