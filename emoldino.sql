-- MySQL dump 10.13  Distrib 8.0.27, for macos11 (x86_64)
--
-- Host: 127.0.0.1    Database: emoldino
-- ------------------------------------------------------
-- Server version	8.0.23

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `TB_RAW`
--

DROP TABLE IF EXISTS `TB_RAW`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `TB_RAW` (
  `read_date` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `rawdata` longtext,
  `delivered` tinyint DEFAULT '0',
  PRIMARY KEY (`read_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TB_RAW`
--

LOCK TABLES `TB_RAW` WRITE;
/*!40000 ALTER TABLE `TB_RAW` DISABLE KEYS */;
INSERT INTO `TB_RAW` VALUES ('2021-12-03 08:06:55.988','CDATA/SC_0000005/20211130103612/20211130113452/00032/9/4/043304410445044704470447/0212/10961100/ab30/ADATA/SC_0000005/00006/20211130104521/0.3,0.024,0.5,0.057,1.5,0.048,2.6,0.045,20.6,0.012,86.8,0.030,87.1,0.497,87.7,0.008/20211130105432/0.3,0.020,0.5,0.048,1.5,0.047,2.6,0.039,5.7,0.012,15.9,0.006,20.4,0.012,86.8,0.040/20211130110341/0.3,0.021,0.5,0.044,1.5,0.053,2.7,0.042,5.7,0.006,20.5,0.016,86.8,0.035,87.1,0.496/20211130111251/0.3,0.017,0.5,0.061,1.5,0.052,2.7,0.045,5.7,0.005,13.5,0.008,20.4,0.013,86.8,0.023/20211130112201/0.3,0.023,0.5,0.049,1.5,0.055,2.7,0.046,20.2,0.012,86.8,0.033,87.1,0.000,87.7,0.010/20211130113111/0.3,0.017,0.5,0.048,1.5,0.046,2.7,0.047,5.7,0.008,15.7,0.010,20.3,0.013,86.8,0.027/TEST/0.0/88.4/109.6/198.4/219.6/308.4/329.6/418.4/439.6/528.4/549.6/638.5/659.6/748.4/769.6/858.4/879.6/968.4/989.6/1078.4/1099.6/1188.4/1209.6/1298.5/1319.6/1408.4/1429.6/1518.4/1539.6/1628.4/1649.6/1738.4/1759.6/1848.4/1869.6/1958.5/1979.6/2068.4/2089.6/2178.4/2199.6/2288.4/2309.6/2398.4/2419.6/2508.4/2529.6/2618.5/2639.6/2728.4/2749.6/2838.4/2859.6/2948.4/2969.6/3058.4/3079.6/3168.4/3189.6/3278.5/3299.6/3388.4/3409.6/3498.4',1),('2021-12-03 08:07:28.341','CDATA/SC_0000005/20211130123711/20211130133552/00032/9/4/045504560456045704580457/0212/109811001102/b12cb4ab3cab8/ADATA/SC_0000005/00006/20211130124621/0.3,0.015,0.6,0.051,1.5,0.057,2.7,0.041,5.8,0.004,20.5,0.011,86.8,0.024,87.2,0.000/20211130125531/0.3,0.020,0.6,0.033,1.5,0.061,2.7,0.042,5.7,0.011,20.5,0.012,86.8,0.025,87.2,0.000/20211130130442/0.1,0.015,0.4,0.040,1.3,0.054,2.5,0.045,5.5,0.008,20.3,0.013,86.6,0.021,87.0,0.000/20211130131352/0.3,0.020,0.6,0.042,1.5,0.058,2.7,0.045,5.8,0.005,13.1,0.009,20.5,0.014,86.8,0.025/20211130132301/0.3,0.018,0.6,0.042,1.5,0.055,2.7,0.038,5.7,0.007,20.6,0.013,86.8,0.019,87.2,0.000/20211130133211/0.3,0.020,0.6,0.050,1.5,0.059,2.7,0.041,5.7,0.009,20.6,0.013,86.9,0.023,87.2,0.000/TEST/0.0/88.8/110.0/198.8/220.0/308.8/330.0/418.8/440.0/528.8/550.0/638.9/660.0/748.8/770.0/858.8/880.0/968.8/990.0/1078.8/1100.0/1188.8/1210.0/1298.9/1320.0/1408.8/1430.2/1518.8/1540.2/1628.8/1650.2/1738.8/1760.2/1848.8/1870.2/1958.9/1980.0/2068.8/2090.0/2178.8/2200.0/2288.8/2310.0/2398.8/2420.2/2508.8/2530.0/2618.9/2640.0/2728.8/2750.0/2838.8/2860.0/2948.8/2970.0/3058.8/3080.0/3168.8/3190.0/3278.9/3300.0/3388.8/3410.0/3498.8',1),('2021-12-03 08:07:39.352','CDATA/SC_0000005/20211130133742/20211130143623/00032/9/4/045704580458046004580461/0212/109811001102/b3cb2acb23/ADATA/SC_0000005/00006/20211130134653/0.1,0.021,0.4,0.029,1.3,0.059,2.5,0.043,5.5,0.006,20.2,0.014,86.7,0.018,87.0,0.000/20211130135603/0.1,0.015,0.5,0.021,1.3,0.057,2.5,0.041,5.5,0.008,20.2,0.014,86.7,0.023,87.0,0.000/20211130140513/0.1,0.014,0.4,0.042,1.4,0.061,2.5,0.042,5.5,0.007,15.9,0.008,20.3,0.012,86.7,0.020/20211130141422/0.1,0.014,0.5,0.024,1.4,0.051,2.5,0.040,5.5,0.007,13.0,0.010,20.3,0.012,86.7,0.019/20211130142332/0.1,0.014,0.4,0.022,1.4,0.055,2.5,0.040,5.5,0.008,20.2,0.015,86.7,0.021,87.0,0.000/20211130143243/0.1,0.019,0.4,0.039,1.4,0.056,2.5,0.047,5.5,0.011,17.1,0.009,20.4,0.013,86.7,0.020/TEST/0.0/88.8/110.0/198.8/220.0/308.8/330.0/418.8/440.2/528.8/550.2/638.9/660.2/748.8/770.0/858.8/880.2/968.8/990.2/1078.8/1100.2/1188.8/1210.2/1298.9/1320.2/1408.8/1430.2/1518.8/1540.2/1628.8/1650.2/1738.8/1760.2/1848.8/1870.2/1958.9/1980.2/2068.8/2090.2/2178.8/2200.2/2288.8/2310.2/2398.8/2420.2/2508.8/2530.2/2618.9/2640.2/2728.8/2750.2/2838.8/2860.2/2948.8/2970.2/3058.8/3080.2/3168.8/3190.2/3278.9/3300.2/3388.8/3410.2/3498.8',1),('2021-12-03 08:07:49.852','CDATA/SC_0000005/20211130155122/20211130165130/00032/9/4/046504650462046304630465/0212/1100/a31/ADATA/SC_0000005/00006/20211130160031/0.2,0.009,0.5,0.041,1.5,0.055,2.6,0.044,5.6,0.009,13.7,0.011,20.4,0.011,86.8,0.018/20211130160942/0.2,0.012,0.5,0.040,1.5,0.051,2.6,0.041,5.6,0.009,20.3,0.016,86.8,0.019,87.1,0.000/20211130161851/0.2,0.012,0.5,0.041,1.5,0.055,2.6,0.040,5.6,0.012,13.5,0.006,19.1,0.012,20.3,0.014/20211130162801/0.2,0.015,0.5,0.040,1.5,0.057,2.6,0.042,5.6,0.011,11.5,0.005,20.3,0.014,86.8,0.019/20211130163712/0.2,0.014,0.5,0.042,1.5,0.056,2.6,0.042,5.7,0.013,13.1,0.003,20.5,0.014,86.8,0.017/20211130164622/0.2,0.014,0.5,0.049,1.5,0.058,2.6,0.042,5.7,0.015,13.1,0.010,20.2,0.014,86.8,0.018/TEST/0.0/88.8/110.0/198.8/220.0/308.8/330.0/418.8/440.0/528.8/550.0/638.9/660.0/748.8/770.0/858.8/880.0/968.8/990.0/1078.8/1100.0/1188.8/1210.0/1298.9/1320.0/1408.8/1430.0/1518.8/1540.0/1628.8/1650.0/1738.8/1760.0/1848.8/1870.0/1958.9/1980.0/2068.8/2090.0/2178.8/2200.0/2288.8/2310.0/2398.8/2420.0/2508.8/2530.0/2618.9/2640.0/2728.8/2750.0/2838.8/2860.0/2948.8/2970.0/3058.8/3080.0/3168.8/3190.0/3278.9/3300.0/3388.8/3410.0/3498.8',1),('2021-12-03 08:08:01.860','CDATA/SC_0000005/20211130165151/20211130175200/00032/9/4/046304650465046504650463/0212/1100/a31/ADATA/SC_0000005/00006/20211130170101/0.3,0.011,0.5,0.060,1.5,0.057,2.6,0.041,5.7,0.009,15.3,0.003,16.8,0.009,17.1,0.005/20211130171011/0.2,0.016,0.5,0.044,1.5,0.055,2.6,0.044,5.7,0.010,16.7,0.009,20.5,0.015,86.8,0.015/20211130171921/0.2,0.010,0.5,0.036,1.5,0.055,2.6,0.043,5.7,0.013,20.4,0.013,86.8,0.015,87.1,0.000/20211130172832/0.2,0.010,0.5,0.035,1.5,0.056,2.6,0.042,5.7,0.005,13.0,0.008,20.4,0.013,86.8,0.014/20211130173741/0.2,0.011,0.5,0.030,1.5,0.052,2.7,0.043,5.7,0.009,20.5,0.015,86.8,0.014,87.1,0.000/20211130174651/0.2,0.015,0.5,0.045,1.5,0.054,2.7,0.041,5.7,0.010,13.0,0.008,15.7,0.012,20.4,0.011/TEST/0.0/88.8/110.0/198.8/220.0/308.8/330.0/418.8/440.0/528.8/550.0/638.9/660.0/748.8/770.0/858.8/880.0/968.8/990.0/1078.8/1100.0/1188.8/1210.0/1298.9/1320.0/1408.8/1430.0/1518.8/1540.0/1628.8/1650.0/1738.8/1760.0/1848.8/1870.0/1958.9/1980.0/2068.8/2090.0/2178.8/2200.0/2288.8/2310.0/2398.8/2420.0/2508.8/2530.0/2618.9/2640.0/2728.8/2750.0/2838.8/2860.0/2948.8/2970.0/3058.8/3080.0/3168.8/3190.0/3278.9/3300.0/3388.8/3410.0/3498.8',1),('2021-12-03 09:05:46.471','CDATA/SC_0000004/20211203170506/20211203180532/00037/9/4/04170420042004210421/0140/09560958/ab6ab11ab11ab4/ADATA/SC_0000004/00007/20211203171305/0.6,0.082,1.1,0.005,2.6,0.048,3.2,0.021,5.2,0.018,81.0,0.000,81.2,0.022,81.6,0.038/20211203172104/0.7,0.070,1.2,0.005,2.8,0.048,3.3,0.021,5.3,0.020,81.0,0.001,81.3,0.020,81.7,0.035/20211203172903/0.6,0.036,1.1,0.005,2.7,0.049,3.2,0.022,5.2,0.018,80.9,0.006,81.3,0.008,81.6,0.034/20211203173702/0.8,0.079,2.8,0.053,3.3,0.022,5.3,0.018,81.0,0.000,81.3,0.026,81.7,0.035/20211203174501/0.7,0.045,2.7,0.051,3.2,0.022,5.2,0.018,81.0,0.000,81.2,0.025,81.6,0.017/20211203175300/0.6,0.049,2.6,0.050,3.1,0.020,5.2,0.017,81.0,0.026,81.5,0.038/20211203180058/0.7,0.051,2.8,0.049,3.3,0.021,5.3,0.020,81.1,0.000,81.3,0.024,81.7,0.033/TEST/0.0/81.6/95.6/177.4/191.4/273.2/287.2/369.0/383.0/464.8/478.8/560.6/574.6/656.5/670.4/752.2/766.0/848.0/861.8/943.8/957.6/1039.6/1053.4/1135.2/1149.2/1231.0/1245.0/1326.9/1340.8/1422.6/1436.6/1518.4/1532.4/1614.2/1628.2/1710.0/1724.0/1805.8/1819.8/1901.6/1915.4/1997.5/2011.2/2093.2/2107.0/2189.0/2202.8/2284.6/2298.6/2380.4/2394.4/2476.2/2490.2/2572.0/2586.0/2667.9/2681.8/2763.6/2777.6/2859.4/2873.4/2955.2/2969.2/3051.0/3064.8/3146.8/3160.6/3242.6/3256.4/3338.5/3352.2/3434.2/3448.0/3530.0',1),('2021-12-03 10:06:04.920','CDATA/SC_0000004/20211203180545/20211203190545/00009/9/4/042204170388036503480336/0140/09560958/b6ab/ADATA/SC_0000004/00001/20211203181344/0.5,0.031,2.6,0.053,3.1,0.022,5.2,0.019,80.9,0.000,81.1,0.027,81.4,0.033/TEST/0.0/81.8/95.8/177.6/191.6/273.4/287.4/369.2/383.2/465.0/479.0/560.8/574.8/656.7/670.4/752.4/766.2/848.2',1);
/*!40000 ALTER TABLE `TB_RAW` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TB_SENSOR`
--

DROP TABLE IF EXISTS `TB_SENSOR`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `TB_SENSOR` (
  `REG_DATE` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `SENSOR_ID` varchar(256) DEFAULT NULL,
  `READ_TIME` timestamp(3) NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`REG_DATE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TB_SENSOR`
--

LOCK TABLES `TB_SENSOR` WRITE;
/*!40000 ALTER TABLE `TB_SENSOR` DISABLE KEYS */;
INSERT INTO `TB_SENSOR` VALUES ('2021-12-03 02:56:56.668','1','2021-12-03 10:06:04.920'),('2021-12-03 02:56:56.671','2','2021-12-03 02:56:56.671'),('2021-12-03 02:56:56.673','3','2021-12-03 02:56:56.673'),('2021-12-03 02:56:56.675','4','2021-12-03 02:56:56.675'),('2021-12-03 02:56:56.677','5','2021-12-03 02:56:56.677');
/*!40000 ALTER TABLE `TB_SENSOR` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-12-28 16:23:37