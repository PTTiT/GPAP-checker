package com.geocomply.test.gpapchecker

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.geocomply.test.gpapchecker.utils.HashUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppsActivity : AppCompatActivity() {

    private lateinit var compareButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var summaryTextView: TextView
    private lateinit var packagesApiTitleTextView: TextView
    private lateinit var packagesOnlyTextView: TextView
    private lateinit var applicationsApiTitleTextView: TextView
    private lateinit var applicationsOnlyTextView: TextView
    private lateinit var errorTextView: TextView

    private val packageList1: String = "com.coloros.backuprestore,com.sofascore.results,com.google.android.networkstack.tethering,com.mediatek.ims,com.oplus.healthservice,com.whatsapp.w4b,com.android.cts.priv.ctsshim,com.realme.link,com.google.android.youtube,com.android.internal.display.cutout.emulation.corner,com.oplus.smartengine,com.google.android.ext.services,com.coloros.onekeylockscreen,com.android.internal.display.cutout.emulation.double,com.invinciblesstudioltd.soccermanager2025,com.android.providers.telephony,com.oplus.securitykeyboard,com.android.dynsystem,com.oplus.crashbox,com.google.android.googlequicksearchbox,com.heytap.pictorial,com.google.android.cellbroadcastservice,com.coloros.smartsidebar,com.android.providers.calendar,com.google.android.apps.googleassistant,com.mediatek.telephony,br.com.serasaexperian.consumidor,org.telegram.messenger,com.android.providers.media,com.coloros.karaoke,com.android.networkstack.tethering.overlay,com.google.android.onetimeinitializer,com.google.android.ext.shared,com.android.internal.systemui.navbar.gestural_wide_back,com.mediatek.location.lppe.main,com.heytap.music,com.northcube.sleepcycle,com.android.wifi.resources.overlay.oplus,com.oplus.location,com.mgoogle.android.gms,com.mediatek.SettingsProviderResOverlay,org.chromium.webapk.a01d0b1b3803f4d4a_v2,com.mercadopago.wallet,com.mediatek.systemuiresoverlay,com.dominovamos.game,br.com.radios.radiosmobile.radiosnet,com.android.launcher,com.oppoex.afterservice,com.android.networkstack.tethering.inprocess.overlay,com.fromsolvers.r10,com.oppo.operationManual,com.oplus.screenrecorder,com.android.wifi.resources.overlay,com.android.externalstorage,com.android.htmlviewer,com.coloros.healthcheck,br.gov.meugovbr,com.whatsapp,com.alphainventor.filemanager,com.android.companiondevicemanager,com.coloros.weather.service,br.com.dietaetreino,com.android.mms.service,br.bet7k,com.android.providers.downloads,com.oplus.powermonitor,com.android.systemui.overlay.fingerprint.anim.ccyh,com.android.systemui.overlay.fingerprint.anim.jhsy,com.android.systemui.overlay.fingerprint.anim.jslz,com.android.systemui.overlay.fingerprint.anim.lgsy,com.android.systemui.overlay.fingerprint.anim.nlgs,com.android.systemui.overlay.fingerprint.anim.tyjw,com.android.systemui.overlay.fingerprint.anim.xklc,com.google.android.apps.messaging,com.google.android.networkstack.tethering.overlay,com.mediatek.engineermode,com.android.internal.systemui.onehanded.gestural,br.com.gabba.Caixa,com.mediatek.cellbroadcastuiresoverlay,com.oplus.interconnectcollectkit,com.oplus.deepthinker,com.openai.chatgpt,com.oplus.qualityprotect,com.oplus.apprecover,com.microsoft.office.officelens,com.mediatek.omacp,com.primety.sodexomobile,com.android.bluetooth.oplus.overlay,com.android.networkstack.inprocess.overlay,android.frameworkres.overlay.display.product,br.com.bnb.bnbagro,com.oplus.aod,com.oplus.nhs,com.oplus.ota,com.oplus.pay,com.oplus.sau,com.oplus.sos,com.google.android.configupdater,com.microsoft.office.excel,com.coloros.systemclone,com.oplus.exserviceui,com.google.android.providers.media.module,br.com.meupag,com.google.android.overlay.modules.permissioncontroller,com.oplus.romupdate,com.heytap.usercenter,com.coloros.focusmode,com.android.systemui.plugin.globalactions.wallet,br.com.senior.employee,com.google.ar.core,com.google.ar.lens,com.android.providers.downloads.ui,com.android.vending,com.android.pacprocessor,com.android.simappdialog,com.coloros.oshare,br.bet.superbet.sport,com.coloros.scenemode,com.android.internal.display.cutout.emulation.hole,com.android.internal.display.cutout.emulation.tall,com.android.vendors.bridge.softsim,com.android.networkstack.overlay,com.android.certinstaller,com.oplus.framework_bluetooth.overlay,com.android.carrierconfig,com.google.android.marvin.talkback,com.android.internal.systemui.navbar.threebutton,com.coloros.bootreg,android,com.oplus.notificationmanager,br.com.uol.ps.myaccount,com.android.egg,com.android.mtp,com.android.nfc,com.android.ons,com.android.stk,com.oplus.locationproxy,com.android.backupconfirm,com.google.android.cellbroadcastreceiver.overlay,se.dirac.acs,com.instagram.android,com.waze,eu.livesport.FlashScore_com,com.coloros.activation,com.android.statementservice,com.google.android.gm,android.autoinstalls.config.oppo,com.mediatek.mdmlsample,com.realme.securitycheck,com.google.android.overlay.gmsconfig.common,com.android.settings.intelligence,com.mediatek.frameworkresoverlay,com.debug.loggerui,com.google.android.cellbroadcastservice.overlay,com.android.internal.systemui.navbar.gestural_extra_wide_back,com.microsoft.office.outlook,com.google.android.permissioncontroller,com.android.carrierconfig.oplus.overlay,br.jus.tse.eleitoral.etitulo,com.google.android.setupwizard,com.realme.wellbeing,com.android.providers.settings,com.android.sharedstoragebackup,com.facebook.services,com.oplus.eyeprotect,com.android.printspooler,com.taxis99,com.android.hotwordenrollment.okgoogle,app.rvx.android.apps.youtube.music,com.kaizengaming.betano.brazil,com.google.android.overlay.modules.ext.services,com.oplus.synergy,com.coloros.ocs.opencapabilityservice,com.android.se,com.android.inputdevices,com.google.android.apps.wellbeing,com.fido.asm,br.gov.caixa.fgts.trabalhador,com.google.android.dialer,com.socialnmobile.dictapps.notepad.color.note,com.android.bips,com.mediatek,com.google.android.apps.nbu.files,br.gov.datasus.cnsdigital,com.daemon.shelper,com.google.android.captiveportallogin,com.google.android.accessibility.soundamplifier,com.google.android.overlay.gmsconfig.comms,com.oplus.bttestmode,com.adobe.scan.android,com.twitter.android,com.pdfeditor.pdfeditorandriod,com.oplus.customize.coreapp,com.nu.production,com.google.android.apps.docs,com.google.android.apps.maps,com.oplus.uiengine,com.google.android.modulemetadata,com.oplus.statistics.rom,oplus,com.android.cellbroadcastreceiver,com.google.android.webview,android.frameworkres.overlay.Network,com.coloros.weather2,com.google.android.overlay.modules.documentsui,com.google.android.networkstack,br.com.bradseg.bscelular,com.google.android.contactkeys,com.google.android.contacts,com.android.server.telecom,com.google.android.syncadapters.contacts,com.android.keychain,com.copafacil,com.android.chrome,com.coloros.compass2,net.zedge.android,com.oplus.wirelesssettings,com.google.android.packageinstaller,com.google.android.gms,com.google.android.gsf,com.google.android.ims,com.google.android.tts,com.android.wifi.resources,app.revanced.android.gms,com.google.android.apps.walletnfcrel,com.android.calllogbackup,com.lightricks.facetune.free,com.google.android.partnersetup,com.android.cameraextensions,com.android.localtransport,com.google.android.overlay.gmsconfig.gsa,com.android.carrierdefaultapp,org.chromium.webapk.ac8e67ae68dc471b7_v2,com.oplus.uxdesign,com.coloros.lockassistant,br.gov.bnb.nelmobile,com.android.theme.font.notoserifsource,com.mediatek.FrameworkResOverlayExt,com.android.proxyhandler,com.google.android.safetycenter.resources,com.android.internal.display.cutout.emulation.waterfall,com.oplus.appplatform,org.chromium.webapk.a7b7c6dd82fae440c_v2,com.coloros.calculator,com.zhiliaoapp.musically,com.google.android.connectivity.resources,com.google.android.overlay.modules.permissioncontroller.forframework,com.google.android.feedback,com.google.android.printservice.recommendation,com.google.android.apps.photos,com.google.android.calendar,com.android.managedprovisioning,com.spotify.music,com.mercadolibre,com.mediatek.capctrl.service,com.tencent.soter.soterserver,com.google.android.documentsui,com.microsoft.office.officehubrow,com.google.mainline.telemetry,br.com.netshoes.app,com.amazon.avod.thirdpartyclient,com.coloros.alarmclock,com.google.android.permissioncontroller.overlay.oplus,com.oplus.atlas,com.oplus.games,com.oplus.stdid,com.bradesco,br.com.stone.ton,com.android.providers.partnerbookmarks,com.oplus.exsystemservice,com.oplus.sauhelper,br.biblia,com.trustonic.teeservice,com.android.wallpaper.livepicker,com.oplus.wallpapers,com.android.apps.tag,com.oplus.encryption,com.coloros.phonemanager,com.coloros.securepay,com.facebook.system,com.a0soft.gphone.acc.pro,com.zzkko,com.realme.movieshot,br.com.minhavida.progress,com.adobe.reader,com.heytap.colorfulengine,com.oplus.trafficmonitor,com.google.android.networkstack.permissionconfig,com.android.storagemanager,com.picpay,com.android.bookmarkprovider,com.ses.entitlement.o2,com.android.settings,br.com.vivo,com.heytap.mcs,com.oplus.wifibackuprestore,com.google.android.networkstack.overlay,com.oplus.framework.rro.realme,com.oplus.battery,com.strava,com.android.wifi.mainline.resources.overlay,br.gov.dataprev.carteiradigital,com.mediatek.mdmconfig,com.google.android.apps.books,com.google.android.projection.gearhead,com.oplus.multiapp,com.oplus.engineercamera,com.oplus.postmanservice,com.google.android.safetycore,com.mediatek.lbs.em2.ui,com.android.cts.ctsshim,com.oplus.portrait,com.oplus.multimedia.dirac,com.coloros.video,com.google.android.overlay.modules.modulemetadata.forframework,com.heytap.accessory,com.coloros.filemanager,com.nst.smartersplayer,br.com.rdsaude.healthPlatform.android,com.oplus.gesture,com.oplus.securitypermission,com.android.vpndialogs,com.fido.uafclient,com.google.android.keep,com.coloros.soundrecorder,com.android.phone,com.android.shell,com.oplus.cast,com.oplus.cosa,com.oplus.cota,com.oplus.lfeh,com.oplus.onet,com.oplus.athena,com.android.wallpaperbackup,com.android.providers.blockednumber,br.com.tecnonutri.app,com.android.email.partnerprovider,com.android.providers.userdictionary,com.oplus.camera,com.shopee.br,com.metlife.brazil.business.css,com.oppo.quicksearchbox,com.android.hotspot2.osulogin,com.google.android.gms.location.history,com.android.internal.systemui.navbar.gestural,com.android.location.fused,com.android.systemui,com.android.bluetoothmidiservice,com.heytap.themestore,com.sokkerpro.android,com.facebook.appmanager,com.gympass,com.adobe.lrmobile,com.coloros.floatassistant,com.android.carrierconfig.overlay,com.oplus.safecenter,com.coloros.childrenspace,com.android.traceur,com.google.android.cellbroadcastreceiver,com.guca.whatssemcontato,com.oplus.linker,com.oplus.logkit,com.oplus.melody,com.coloros.gallery3d,android.auto_generated_rro_product__,com.google.android.play.games,de.motain.iliga,com.oplus.nrMode,com.tafayor.hibernator,fr.dvilleneuve.lockito,com.android.bluetooth,com.oplus.engineermode,com.android.wallpaperpicker,com.android.providers.contacts,com.oplus.screenshot,com.android.internal.systemui.navbar.gestural_narrow_back,com.google.android.photopicker,com.oplus.subsys,com.microsoft.teams,com.oplus.engineernetwork,com.mediatek.gbaservice,com.wapi.wapicertmanager,com.google.android.inputmethod.latin,br.com.geosapiens.coletumv2,com.glance.internet,com.google.android.apps.restore"
    private val packageList2: String = "com.coloros.backuprestore,com.sofascore.results,com.google.android.networkstack.tethering,com.mediatek.ims,com.oplus.healthservice,com.whatsapp.w4b,com.android.cts.priv.ctsshim,com.realme.link,com.google.android.youtube,com.android.internal.display.cutout.emulation.corner,com.oplus.smartengine,com.google.android.ext.services,com.coloros.onekeylockscreen,com.android.internal.display.cutout.emulation.double,com.invinciblesstudioltd.soccermanager2025,com.android.providers.telephony,com.oplus.securitykeyboard,com.android.dynsystem,com.oplus.crashbox,com.google.android.googlequicksearchbox,com.heytap.pictorial,com.google.android.cellbroadcastservice,com.coloros.smartsidebar,com.android.providers.calendar,com.google.android.apps.googleassistant,com.mediatek.telephony,br.com.serasaexperian.consumidor,org.telegram.messenger,com.android.providers.media,com.coloros.karaoke,com.android.networkstack.tethering.overlay,com.google.android.onetimeinitializer,com.google.android.ext.shared,com.android.internal.systemui.navbar.gestural_wide_back,com.mediatek.location.lppe.main,com.heytap.music,com.northcube.sleepcycle,com.android.wifi.resources.overlay.oplus,com.oplus.location,com.mgoogle.android.gms,com.mediatek.SettingsProviderResOverlay,org.chromium.webapk.a01d0b1b3803f4d4a_v2,com.mercadopago.wallet,com.mediatek.systemuiresoverlay,com.dominovamos.game,br.com.radios.radiosmobile.radiosnet,com.android.launcher,com.oppoex.afterservice,com.android.networkstack.tethering.inprocess.overlay,com.fromsolvers.r10,com.oppo.operationManual,com.oplus.screenrecorder,com.android.wifi.resources.overlay,com.android.externalstorage,com.android.htmlviewer,com.coloros.healthcheck,br.gov.meugovbr,com.whatsapp,com.alphainventor.filemanager,com.android.companiondevicemanager,com.coloros.weather.service,br.com.dietaetreino,com.android.mms.service,br.bet7k,com.android.providers.downloads,com.oplus.powermonitor,com.android.systemui.overlay.fingerprint.anim.ccyh,com.android.systemui.overlay.fingerprint.anim.jhsy,com.android.systemui.overlay.fingerprint.anim.jslz,com.android.systemui.overlay.fingerprint.anim.lgsy,com.android.systemui.overlay.fingerprint.anim.nlgs,com.android.systemui.overlay.fingerprint.anim.tyjw,com.android.systemui.overlay.fingerprint.anim.xklc,com.google.android.apps.messaging,com.google.android.networkstack.tethering.overlay,com.mediatek.engineermode,com.android.internal.systemui.onehanded.gestural,br.com.gabba.Caixa,com.mediatek.cellbroadcastuiresoverlay,com.oplus.interconnectcollectkit,com.oplus.deepthinker,com.openai.chatgpt,com.oplus.qualityprotect,com.oplus.apprecover,com.microsoft.office.officelens,com.mediatek.omacp,com.primety.sodexomobile,com.android.bluetooth.oplus.overlay,com.android.networkstack.inprocess.overlay,android.frameworkres.overlay.display.product,br.com.bnb.bnbagro,com.oplus.aod,com.oplus.nhs,com.oplus.ota,com.oplus.pay,com.oplus.sau,com.oplus.sos,com.google.android.configupdater,com.microsoft.office.excel,com.coloros.systemclone,com.oplus.exserviceui,com.google.android.providers.media.module,br.com.meupag,com.google.android.overlay.modules.permissioncontroller,com.oplus.romupdate,com.heytap.usercenter,com.coloros.focusmode,com.android.systemui.plugin.globalactions.wallet,br.com.senior.employee,com.google.ar.core,com.google.ar.lens,com.android.providers.downloads.ui,com.android.vending,com.android.pacprocessor,com.android.simappdialog,com.coloros.oshare,br.bet.superbet.sport,com.coloros.scenemode,com.android.internal.display.cutout.emulation.hole,com.android.internal.display.cutout.emulation.tall,com.android.vendors.bridge.softsim,com.android.networkstack.overlay,com.android.certinstaller,com.oplus.framework_bluetooth.overlay,com.android.carrierconfig,com.google.android.marvin.talkback,com.android.internal.systemui.navbar.threebutton,com.coloros.bootreg,android,com.oplus.notificationmanager,br.com.uol.ps.myaccount,com.android.egg,com.android.mtp,com.android.nfc,com.android.ons,com.android.stk,com.oplus.locationproxy,com.android.backupconfirm,com.google.android.cellbroadcastreceiver.overlay,se.dirac.acs,com.instagram.android,com.waze,eu.livesport.FlashScore_com,com.coloros.activation,com.android.statementservice,com.google.android.gm,android.autoinstalls.config.oppo,com.mediatek.mdmlsample,com.realme.securitycheck,com.google.android.overlay.gmsconfig.common,com.android.settings.intelligence,com.mediatek.frameworkresoverlay,com.debug.loggerui,com.google.android.cellbroadcastservice.overlay,com.android.internal.systemui.navbar.gestural_extra_wide_back,com.microsoft.office.outlook,com.google.android.permissioncontroller,com.android.carrierconfig.oplus.overlay,br.jus.tse.eleitoral.etitulo,com.google.android.setupwizard,com.realme.wellbeing,com.android.providers.settings,com.android.sharedstoragebackup,com.facebook.services,com.oplus.eyeprotect,com.android.printspooler,com.taxis99,com.android.hotwordenrollment.okgoogle,app.rvx.android.apps.youtube.music,com.kaizengaming.betano.brazil,com.google.android.overlay.modules.ext.services,com.oplus.synergy,com.coloros.ocs.opencapabilityservice,com.android.se,com.android.inputdevices,com.google.android.apps.wellbeing,com.fido.asm,br.gov.caixa.fgts.trabalhador,com.google.android.dialer,com.socialnmobile.dictapps.notepad.color.note,com.android.bips,com.mediatek,com.google.android.apps.nbu.files,br.gov.datasus.cnsdigital,com.daemon.shelper,com.google.android.captiveportallogin,com.google.android.accessibility.soundamplifier,com.google.android.overlay.gmsconfig.comms,com.oplus.bttestmode,com.adobe.scan.android,com.twitter.android,com.pdfeditor.pdfeditorandriod,com.oplus.customize.coreapp,com.nu.production,com.google.android.apps.docs,com.google.android.apps.maps,com.oplus.uiengine,com.google.android.modulemetadata,com.oplus.statistics.rom,oplus,com.android.cellbroadcastreceiver,com.google.android.webview,android.frameworkres.overlay.Network,com.coloros.weather2,com.google.android.overlay.modules.documentsui,com.google.android.networkstack,br.com.bradseg.bscelular,com.google.android.contactkeys,com.google.android.contacts,com.android.server.telecom,com.google.android.syncadapters.contacts,com.android.keychain,com.copafacil,com.android.chrome,com.coloros.compass2,net.zedge.android,com.oplus.wirelesssettings,com.google.android.packageinstaller,com.google.android.gms,com.google.android.gsf,com.google.android.ims,com.google.android.tts,com.android.wifi.resources,app.revanced.android.gms,com.google.android.apps.walletnfcrel,com.android.calllogbackup,com.lightricks.facetune.free,com.google.android.partnersetup,com.android.cameraextensions,com.android.localtransport,com.google.android.overlay.gmsconfig.gsa,com.android.carrierdefaultapp,org.chromium.webapk.ac8e67ae68dc471b7_v2,com.oplus.uxdesign,com.coloros.lockassistant,br.gov.bnb.nelmobile,com.android.theme.font.notoserifsource,com.mediatek.FrameworkResOverlayExt,com.android.proxyhandler,com.google.android.safetycenter.resources,com.android.internal.display.cutout.emulation.waterfall,com.oplus.appplatform,org.chromium.webapk.a7b7c6dd82fae440c_v2,com.coloros.calculator,com.zhiliaoapp.musically,com.google.android.connectivity.resources,com.google.android.overlay.modules.permissioncontroller.forframework,com.google.android.feedback,com.google.android.printservice.recommendation,com.google.android.apps.photos,com.google.android.calendar,com.android.managedprovisioning,com.spotify.music,com.mercadolibre,com.mediatek.capctrl.service,com.tencent.soter.soterserver,com.google.android.documentsui,com.microsoft.office.officehubrow,com.google.mainline.telemetry,br.com.netshoes.app,com.amazon.avod.thirdpartyclient,com.coloros.alarmclock,com.google.android.permissioncontroller.overlay.oplus,com.oplus.atlas,com.oplus.games,com.oplus.stdid,com.bradesco,br.com.stone.ton,com.android.providers.partnerbookmarks,com.oplus.exsystemservice,com.oplus.sauhelper,br.biblia,com.trustonic.teeservice,com.android.wallpaper.livepicker,com.oplus.wallpapers,com.android.apps.tag,com.oplus.encryption,com.coloros.phonemanager,com.coloros.securepay,com.facebook.system,com.a0soft.gphone.acc.pro,com.zzkko,com.realme.movieshot,br.com.minhavida.progress,com.adobe.reader,com.heytap.colorfulengine,com.oplus.trafficmonitor,com.google.android.networkstack.permissionconfig,com.android.storagemanager,com.picpay,com.android.bookmarkprovider,com.ses.entitlement.o2,com.android.settings,br.com.vivo,com.heytap.mcs,com.oplus.wifibackuprestore,com.google.android.networkstack.overlay,com.oplus.framework.rro.realme,com.oplus.battery,com.strava,com.android.wifi.mainline.resources.overlay,br.gov.dataprev.carteiradigital,com.mediatek.mdmconfig,com.google.android.apps.books,com.google.android.projection.gearhead,com.oplus.multiapp,com.oplus.engineercamera,com.oplus.postmanservice,com.google.android.safetycore,com.mediatek.lbs.em2.ui,com.android.cts.ctsshim,com.oplus.portrait,com.oplus.multimedia.dirac,com.coloros.video,com.google.android.overlay.modules.modulemetadata.forframework,com.heytap.accessory,com.coloros.filemanager,com.nst.smartersplayer,br.com.rdsaude.healthPlatform.android,com.oplus.gesture,com.oplus.securitypermission,com.android.vpndialogs,com.fido.uafclient,com.google.android.keep,com.coloros.soundrecorder,com.android.phone,com.android.shell,com.oplus.cast,com.oplus.cosa,com.oplus.cota,com.oplus.lfeh,com.oplus.onet,com.oplus.athena,com.android.wallpaperbackup,com.android.providers.blockednumber,br.com.tecnonutri.app,com.android.email.partnerprovider,com.android.providers.userdictionary,com.oplus.camera,com.shopee.br,com.metlife.brazil.business.css,com.oppo.quicksearchbox,com.android.hotspot2.osulogin,com.google.android.gms.location.history,com.android.internal.systemui.navbar.gestural,com.android.location.fused,com.android.systemui,com.android.bluetoothmidiservice,com.heytap.themestore,com.sokkerpro.android,com.facebook.appmanager,com.gympass,com.adobe.lrmobile,com.coloros.floatassistant,com.android.carrierconfig.overlay,com.oplus.safecenter,com.coloros.childrenspace,com.android.traceur,com.google.android.cellbroadcastreceiver,com.guca.whatssemcontato,com.oplus.linker,com.oplus.logkit,com.oplus.melody,com.coloros.gallery3d,android.auto_generated_rro_product__,com.google.android.play.games,de.motain.iliga,com.oplus.nrMode,com.tafayor.hibernator,com.android.bluetooth,com.oplus.engineermode,com.android.wallpaperpicker,com.android.providers.contacts,com.oplus.screenshot,com.android.internal.systemui.navbar.gestural_narrow_back,com.google.android.photopicker,com.oplus.subsys,com.microsoft.teams,com.oplus.engineernetwork,com.mediatek.gbaservice,com.wapi.wapicertmanager,com.google.android.inputmethod.latin,br.com.geosapiens.coletumv2,com.glance.internet,com.google.android.apps.restore"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        compareButton = findViewById(R.id.btn_compare_packages)
        progressBar = findViewById(R.id.progress_bar)
        summaryTextView = findViewById(R.id.tv_summary)
        packagesApiTitleTextView = findViewById(R.id.tv_packages_api_title)
        packagesOnlyTextView = findViewById(R.id.tv_packages_only)
        applicationsApiTitleTextView = findViewById(R.id.tv_applications_api_title)
        applicationsOnlyTextView = findViewById(R.id.tv_applications_only)
        errorTextView = findViewById(R.id.tv_error)
    }

    private fun setupClickListeners() {
        compareButton.setOnClickListener {
            comparePackageApis()
        }
    }

    private fun comparePackageApis() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                hideResults()

                val packagesFromPackageInfo = withContext(Dispatchers.IO) {
                    getInstalledPackages()
                }

                val packagesFromApplicationInfo = withContext(Dispatchers.IO) {
                    getInstalledApplicationPackages()
                }

                val comparison =
                    comparePackageLists(packagesFromPackageInfo, packagesFromApplicationInfo)

                val hash1 = createSha256Hash(packageList1)
                val hash2 = createSha256Hash(packageList2)

                showResults(comparison, hash1, hash2)

            } catch (e: Exception) {
                showError(getString(R.string.error_loading_packages, e.message))
            } finally {
                showLoading(false)
            }
        }
    }

    private fun getInstalledPackages(): List<String> {
        return try {
            val flag =
                PackageManager.GET_PERMISSIONS or PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES

            packageManager.getInstalledPackages(flag)
                .map { it.packageName }
                .sorted()
        } catch (e: Exception) {
            throw Exception("getInstalledPackages failed: ${e.message}")
        }
    }

    private fun getInstalledApplicationPackages(): List<String> {
        return try {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .map { it.packageName }
                .sorted()
        } catch (e: Exception) {
            throw Exception("getInstalledApplications failed: ${e.message}")
        }
    }

    private fun comparePackageLists(
        packagesApiList: List<String>,
        applicationsApiList: List<String>
    ): PackageComparison {
        val packagesSet = packagesApiList.toSet()
        val applicationsSet = applicationsApiList.toSet()

        val onlyInPackages = packagesSet - applicationsSet
        val onlyInApplications = applicationsSet - packagesSet
        val common = packagesSet.intersect(applicationsSet)

        return PackageComparison(
            onlyInPackages = onlyInPackages.sorted(),
            onlyInApplications = onlyInApplications.sorted(),
            commonCount = common.size,
            packagesTotal = packagesApiList.size,
            applicationsTotal = applicationsApiList.size
        )
    }

    private fun showResults(comparison: PackageComparison, hash1: String, hash2: String) {
        // Show summary
        val summaryText = getString(
            R.string.common_packages,
            comparison.commonCount
        ) + "\n" +
                "getInstalledPackages(): ${comparison.packagesTotal} total\n" +
                "getInstalledApplications(): ${comparison.applicationsTotal} total\n" +
                "hash1: ${hash1}\n" +
                "hash2: ${hash2}"

        summaryTextView.text = summaryText
        summaryTextView.visibility = View.VISIBLE

        // Show packages only in PackageInfo API
        if (comparison.onlyInPackages.isNotEmpty()) {
            packagesApiTitleTextView.text =
                getString(R.string.only_in_packages, comparison.onlyInPackages.size)
            packagesApiTitleTextView.visibility = View.VISIBLE

            packagesOnlyTextView.text = comparison.onlyInPackages.joinToString("\n")
            packagesOnlyTextView.visibility = View.VISIBLE
        }

        // Show packages only in ApplicationInfo API
        if (comparison.onlyInApplications.isNotEmpty()) {
            applicationsApiTitleTextView.text =
                getString(R.string.only_in_applications, comparison.onlyInApplications.size)
            applicationsApiTitleTextView.visibility = View.VISIBLE

            applicationsOnlyTextView.text = comparison.onlyInApplications.joinToString("\n")
            applicationsOnlyTextView.visibility = View.VISIBLE
        }

        errorTextView.visibility = View.GONE
    }

    private fun hideResults() {
        summaryTextView.visibility = View.GONE
        packagesApiTitleTextView.visibility = View.GONE
        packagesOnlyTextView.visibility = View.GONE
        applicationsApiTitleTextView.visibility = View.GONE
        applicationsOnlyTextView.visibility = View.GONE
        errorTextView.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        compareButton.isEnabled = !isLoading

        if (isLoading) {
            compareButton.text = getString(R.string.loading_packages)
        } else {
            compareButton.text = getString(R.string.btn_compare_packages)
        }
    }

    private fun showError(message: String) {
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
        hideResults()
    }

    /**
     * Creates a SHA-256 hash in Base64 format of the input string
     * 
     * @param input The string to hash
     * @return Base64 encoded SHA-256 hash
     */
    private fun createSha256Hash(input: String): String {
        return HashUtils.createSha256Base64Hash(input)
    }
    
    data class PackageComparison(
        val onlyInPackages: List<String>,
        val onlyInApplications: List<String>,
        val commonCount: Int,
        val packagesTotal: Int,
        val applicationsTotal: Int
    )
}