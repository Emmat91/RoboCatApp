if fatload mmc 0 82000000 uImage
then
 echo ***** Kernel: /dev/mmcblk0p1/uImage *****
fi
echo ***** RootFS: /dev/mmcblk0p2 *****
setenv bootargs 'console=ttyO2,115200n8 androidboot.console=ttyO2 mem=256M root=/dev/mmcblk0p2 rw rootfstype=ext4 rootwait init=/init ip=off omap_vout.vid1_static_vrfb_alloc=y vram=8M omapfb.vram=0:8M omapdss.def_disp=lcd omapfb.mode=dvi:800x480-16 omapfb.tiler=y omapfb.mirror=y'







bootm 0x82000000
