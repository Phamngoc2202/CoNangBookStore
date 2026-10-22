import 'package:flutter/material.dart';

import 'home_page.dart';
import 'services/auth_service.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const BookstoreApp());
}

class BookstoreApp extends StatefulWidget {
  const BookstoreApp({super.key});

  @override
  State<BookstoreApp> createState() => _BookstoreAppState();
}

class _BookstoreAppState extends State<BookstoreApp> {
  final _auth = AuthService();
  bool? _loggedIn;

  @override
  void initState() {
    super.initState();
    _restoreSession();
  }

  Future<void> _restoreSession() async {
    final result = await _auth.isLoggedIn();
    if (mounted) setState(() => _loggedIn = result);
  }

  @override
  Widget build(BuildContext context) => MaterialApp(
        title: 'Bookstore',
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: Colors.indigo),
          useMaterial3: true,
        ),
        home: _loggedIn == null
            ? const Scaffold(body: Center(child: CircularProgressIndicator()))
            : _loggedIn!
                ? HomePage(
                    onLogout: () async {
                      await _auth.logout();
                      if (mounted) setState(() => _loggedIn = false);
                    },
                  )
                : LoginPage(onLogin: () => setState(() => _loggedIn = true)),
      );
}

class LoginPage extends StatefulWidget {
  const LoginPage({super.key, required this.onLogin});

  final VoidCallback onLogin;

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _formKey = GlobalKey<FormState>();
  final _username = TextEditingController();
  final _password = TextEditingController();
  bool _busy = false;
  String? _error;

  @override
  void dispose() {
    _username.dispose();
    _password.dispose();
    super.dispose();
  }

  Future<void> _login() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      await AuthService().login(
        username: _username.text,
        password: _password.text,
      );
      if (mounted) widget.onLogin();
    } catch (e) {
      if (mounted) {
        setState(() => _error = e.toString().replaceFirst('Exception: ', ''));
      }
    } finally {
      if (mounted) setState(() => _busy = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Đăng nhập')),
        body: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 420),
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Form(
                key: _formKey,
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    TextFormField(
                      controller: _username,
                      decoration: const InputDecoration(
                          labelText: 'Tên đăng nhập hoặc email'),
                      validator: (value) =>
                          value == null || value.trim().isEmpty
                              ? 'Hãy nhập tên đăng nhập'
                              : null,
                    ),
                    const SizedBox(height: 12),
                    TextFormField(
                      controller: _password,
                      obscureText: true,
                      decoration: const InputDecoration(labelText: 'Mật khẩu'),
                      validator: (value) => value == null || value.isEmpty
                          ? 'Hãy nhập mật khẩu'
                          : null,
                      onFieldSubmitted: (_) {
                        if (!_busy) _login();
                      },
                    ),
                    if (_error != null) ...[
                      const SizedBox(height: 12),
                      Text(_error!,
                          style: TextStyle(
                              color: Theme.of(context).colorScheme.error)),
                    ],
                    const SizedBox(height: 20),
                    FilledButton(
                      onPressed: _busy ? null : _login,
                      child: _busy
                          ? const SizedBox(
                              width: 18,
                              height: 18,
                              child: CircularProgressIndicator(strokeWidth: 2))
                          : const Text('Đăng nhập'),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      );
}
