import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:connectivity_plus/connectivity_plus.dart';

void main() => runApp(const SpeedMeterApp());

class SpeedMeterApp extends StatelessWidget {
  const SpeedMeterApp({super.key});
  @override
  Widget build(BuildContext context) => MaterialApp(
    debugShowCheckedModeBanner: false,
    title: 'عداد سرعة الإنترنت',
    theme: ThemeData(useMaterial3: true, colorSchemeSeed: Colors.blue),
    home: const HomePage(),
  );
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});
  @override State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  static const channel = MethodChannel('internet_speed_meter/service');
  String network = 'جارٍ التحقق...';
  bool running = false;
  double download = 0;
  double upload = 0;
  double totalSpeed = 0;

  @override
  void initState() {
    super.initState();
    _refreshNetwork();
    channel.setMethodCallHandler((call) async {
      if (call.method == 'speedUpdate') {
        final data = Map<String, dynamic>.from(call.arguments as Map);
        if (!mounted) return;
        setState(() {
          download = (data['download'] as num).toDouble();
          upload = (data['upload'] as num).toDouble();
          totalSpeed = download + upload;
        });
      }
    });

    // Automatically start the service
    _start();
  }

  Future<void> _refreshNetwork() async {
    final result = await Connectivity().checkConnectivity();
    if (!mounted) return;
    setState(() {
      if (result.contains(ConnectivityResult.wifi)) network = 'Wi‑Fi';
      else if (result.contains(ConnectivityResult.mobile)) network = 'بيانات الهاتف';
      else network = 'غير متصل';
    });
  }

  Future<void> _start() async {
    await channel.invokeMethod('startService');
    if (mounted) setState(() => running = true);
  }

  Future<void> _stop() async {
    await channel.invokeMethod('stopService');
    if (mounted) setState(() => running = false);
  }

  String formatSpeed(double speedKbps) {
    if (speedKbps >= 1024) {
      return '${(speedKbps / 1024).toStringAsFixed(1)} MB/s';
    } else {
      return '${speedKbps.toStringAsFixed(1)} KB/s';
    }
  }

  @override
  Widget build(BuildContext context) => Directionality(
    textDirection: TextDirection.rtl,
    child: Scaffold(
      appBar: AppBar(title: const Text('عداد سرعة الإنترنت'), centerTitle: true),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Card(
            color: Theme.of(context).colorScheme.primaryContainer,
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                children: [
                  const Text('السرعة الإجمالية', style: TextStyle(fontSize: 18)),
                  const SizedBox(height: 8),
                  Text(
                    formatSpeed(totalSpeed),
                    style: const TextStyle(fontSize: 36, fontWeight: FontWeight.bold),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          Card(child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(children: [
              const Icon(Icons.wifi, size: 56),
              const SizedBox(height: 8),
              Text(network, style: Theme.of(context).textTheme.titleLarge),
              const Text('حالة الاتصال'),
            ]),
          )),
          const SizedBox(height: 16),
          Row(children: [
            Expanded(child: SpeedCard(title: 'تحميل', speedText: formatSpeed(download), icon: Icons.download)),
            const SizedBox(width: 12),
            Expanded(child: SpeedCard(title: 'رفع', speedText: formatSpeed(upload), icon: Icons.upload)),
          ]),
          const SizedBox(height: 20),
          FilledButton.icon(
            onPressed: running ? _stop : _start,
            icon: Icon(running ? Icons.stop : Icons.play_arrow),
            label: Text(running ? 'إيقاف عداد الإشعارات' : 'تشغيل عداد الإشعارات'),
          ),
          const SizedBox(height: 12),
          OutlinedButton.icon(
            onPressed: _refreshNetwork,
            icon: const Icon(Icons.refresh),
            label: const Text('تحديث الاتصال'),
          ),
        ],
      ),
    ),
  );
}

class SpeedCard extends StatelessWidget {
  final String title; final String speedText; final IconData icon;
  const SpeedCard({super.key, required this.title, required this.speedText, required this.icon});
  @override
  Widget build(BuildContext context) => Card(child: Padding(
    padding: const EdgeInsets.symmetric(vertical: 22, horizontal: 8),
    child: Column(children: [
      Icon(icon, size: 30), const SizedBox(height: 8), Text(title),
      const SizedBox(height: 6),
      Text(speedText, style: Theme.of(context).textTheme.titleLarge),
    ]),
  ));
}
