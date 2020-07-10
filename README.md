# BlueBridge
Tunnel tcp communications through bluetooth

# How does it work
<p>In the practice, Bluebridge allows you to connect to a desire tcp port on your raspberry pi from your android without the need of being in the same local network</p>

# Limitations
1. Bluebridge currently only supports one connection at a time per tunnel. (Watch TODO to add more)
2. When tunneling an ssh connection if there is a lot of traffic a bad packet length error is thrown.
# How to use it
1. Install BlueBridgeRP on your Raspberry pi from: https://github.com/vik0t0r/BlueBridgeRP.git 
2. Build and run Bluebridge app
3. By default BlueBridge relays the connection to the port 8022, but you can change this behaviour looking at the TODO
4. Connect to port 8022 from another app (like termux) and enjoy your connection!!
