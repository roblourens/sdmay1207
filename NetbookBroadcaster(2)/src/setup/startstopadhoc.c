#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/syscall.h>
#include <unistd.h>
#include <fcntl.h>

int file_exists(const char* fileName) {
	FILE *file =NULL;
	if(!(file =fopen(fileName, "r"))) {
		return -1;
	}
	return 0;
}

int rmmod(const char * modname) {
	return syscall(__NR_delete_module, modname, O_NONBLOCK  | O_EXCL);
}

int startwifi(const int * phoneType, const char * ip[]) {

	switch (*phoneType) {	
		case 0: //NEXSUS
		system("insmod /system/lib/modules/bcm4329.ko");
		system("ifconfig eth0 %s netmask 255.255.255.0", *ip);
		system("ifconfig eth0 up");
		system("iwconfig eth0 mode ad-hoc");
		system("iwconfig eth0 essid nexusbac");
		system("iwconfig eth0 channel6");
		system("iwconfig eth0 commit");
		break;

		case 1: //HERO
		system("insmod /system/lib/modules/wlan.ko");
		system("wlan_loader -f /system/etc/wifi/Fw1251r1c.bin -e /proc/calibration -i /data/local/bin/tiwlan.ini");
		system("ifconfig rmnet0 %s netmask 255.255.255.0", *ip);
		system("ifconfig rmnet0 up");
		break;

		default:
		return -1;
	}
	return 0;
}

int stopwifi(const int * phoneType) {

	switch (*phoneType) {
		case 0: //NEXUS
		system("ifconfig eth0 down");
		system("killalliwconfig");
		system("rmmod bcm4329");
		break;

		case 1: //HERO
		system("ifconfig rmnet0 down");
		rmmod("wlan");
		break;

		default:
		return -1;
	}
	return 0;
}

int main(int argc, char * argv[]) {

	if(argc != 3 && strcmp(argv[1], "start")) {
		return -1;
	} else if(argc != 2 && strcmp(argv[1], "stop")){
		return -1;
	}

	int phoneType =atoi(argv[2]);
	char * ip[] =argv[3];


	if(strcmp(argv[1], "start") ==0) {
		return startwifi(&phoneType, &ip[]);
	} else if(strcmp(argv[1], "stop") ==0) {
		return stopwifi(&phoneType);
	}
	return -1;
}
