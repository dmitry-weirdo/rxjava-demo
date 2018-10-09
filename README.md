rxjava demo.

Based on the tutorial series here &mdash; https://www.youtube.com/watch?v=1j9VN77DMTk&index=1&list=PLC-8dKj3F0NUvJhdqnzhy2754GbGOu823.

To run, simply execute `gradle run -q` or execute `App` class in IntelliJ IDEA.
Input the digits and the i-th fibonacci number will be output after each.
To stop the app, press `Ctrl-C`.

### Known issues
* App will fail on incorrect input (non-number or less than zero)
* Fibonacci numbers are now just `Integer` based, therefore it will overflow starting from some number.