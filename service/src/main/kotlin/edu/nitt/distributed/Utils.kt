package edu.nitt.distributed

import io.reactivex.Flowable
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface

val ipAddressFlowable: Flowable<InetAddress> =
    Flowable.fromIterable(NetworkInterface.getNetworkInterfaces().toList())
        .filter { !it.isLoopback && it.isUp }
        .flatMap { Flowable.fromIterable(it.inetAddresses.toList()) }
        .filter { it !is Inet6Address }