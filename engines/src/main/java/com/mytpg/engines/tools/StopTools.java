package com.mytpg.engines.tools;

import android.location.Location;

import com.mytpg.engines.entities.Line;
import com.mytpg.engines.entities.core.EntityWithNameAndLocation;
import com.mytpg.engines.entities.stops.PhysicalStop;
import com.mytpg.engines.entities.stops.Stop;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by stalker-mac on 13.11.14.
 */
public abstract class StopTools {

    public static String getRealStopName(final String ArgStopName) {
        String realName = ArgStopName;
        if (ArgStopName.equalsIgnoreCase("Palettes-bachet")) {
            realName = "Bachet-de-Pesay";
        }
         else if (ArgStopName.equalsIgnoreCase("Vernier")) {
            realName = "Vernier-Village";
        } else if (ArgStopName.equalsIgnoreCase("Jussy")) {
            realName = "Jussy-Meurets";
        } else if (ArgStopName.equalsIgnoreCase("Lignon")) {
            realName = "Tours Lignon";
        } else if (ArgStopName.equalsIgnoreCase("Ferney") || ArgStopName.equalsIgnoreCase("Ferney-Voltaire")) {
            realName = "Ferney-V.-Mairie";
        } else if (ArgStopName.equalsIgnoreCase("CS La Bécassière")) {
            realName = "CS Bécassière";
        } else if (ArgStopName.equalsIgnoreCase("Thônex-vallard")) {
            realName = "Th-Vallard-Dne";
        } else if (ArgStopName.equalsIgnoreCase("Veyrier-Tournet.")) {
            realName = "Veyr-Tournettes";
        } else if (ArgStopName.equalsIgnoreCase("Valleiry")) {
            realName = "Viry Chef-Lieu";
        } else if (ArgStopName.equalsIgnoreCase("Carouge")) {
            realName = "Carouge-Rondeau";
        } else if (ArgStopName.equalsIgnoreCase("Champel")) {
            realName = "Crêts-de-Champel";
        } else if (ArgStopName.equalsIgnoreCase("Croix-de-Rozon")) {
            realName = "Cr.-de-Rozon-Dne";
        } else if (ArgStopName.equalsIgnoreCase("Gy")) {
            realName = "Gy-Temple";
        } else if (ArgStopName.equalsIgnoreCase("Gy - Corsinge")) {
            realName = "Corsinge-Village";
        } else if (ArgStopName.equalsIgnoreCase("Sainte-Clotilde")) {
            realName = "Ste-Clotilde";
        } else if (ArgStopName.equalsIgnoreCase("Hôpital la tour")) {
            realName = "Hôpital-La Tour";
        } else if (ArgStopName.equalsIgnoreCase("Bourg d'en haut")) {
            realName = "Collonges-bourg";
        } else if (ArgStopName.equalsIgnoreCase("C.o Renard") || ArgStopName.equalsIgnoreCase("Co Renard")) {
            realName = "C.O. Renard";
        } else if (ArgStopName.equalsIgnoreCase("Challex-La-Halle")) {
            realName = "P+R Challex-Hal.";
        } else if (ArgStopName.equalsIgnoreCase("C.o. seymaz")) {
            realName = "Seymaz";
        } else if (ArgStopName.equalsIgnoreCase("L. international")) {
            realName = "Lycée Internat.";
        } else if (ArgStopName.equalsIgnoreCase("Zi les moulins")) {
            realName = "Z.I. Les Moulins";
        } else if (ArgStopName.equalsIgnoreCase("Bachet")) {
            realName = "Bachet-de-Pesay";
        } else if (ArgStopName.equalsIgnoreCase("La Plaine")) {
            realName = "La Plaine-Gare";
        } else if (ArgStopName.equalsIgnoreCase("Neydens-Vitam")) {
            realName = "Vitam'Parc";
        } else if (ArgStopName.equalsIgnoreCase("Ferney av. jura")) {
            realName = "Avenue du Jura";
        } else if (ArgStopName.equalsIgnoreCase("Veyrier")) {
            realName = "Veyrier-Douane";
        } else if (ArgStopName.equalsIgnoreCase("Thoiry")) {
            realName = "Thoiry-Mairie";
        } else if (ArgStopName.equalsIgnoreCase("Pl.Eaux-Vives")) {
            realName = "Pl. Eaux-Vives";
        } else if (ArgStopName.equalsIgnoreCase("Bel-air - stand")) {
            realName = "Stand";
        } else if (ArgStopName.equalsIgnoreCase("Veigy")) {
            realName = "Veigy-Village";
        } else if (ArgStopName.equalsIgnoreCase("Gex")) {
            realName = "Gex-Aiglette";
        } else if (ArgStopName.equalsIgnoreCase("Puplinge")) {
            realName = "Puplinge-Mairie";
        } else if (ArgStopName.equalsIgnoreCase("Onex-Vallet")) {
            realName = "Vallet";
        } else if (ArgStopName.equalsIgnoreCase("Russin")) {
            realName = "Russin-Village";
        } else if (ArgStopName.equalsIgnoreCase("Hôpitaux")) {
            realName = "Augustins";
        } else if (ArgStopName.equalsIgnoreCase("Palettes - armes")) {
            realName = "Armes";
        } else if (ArgStopName.equalsIgnoreCase("Palettes bachet")) {
            realName = "Bachet-de-Pesay";
        } else if (ArgStopName.equalsIgnoreCase("P bernex")) {
            realName = "P+R Bernex";
        } else if (ArgStopName.equalsIgnoreCase("Geneve-cornavin")) {
            realName = "Gare Cornavin";
        } else if (ArgStopName.equalsIgnoreCase("Coppet")) {
            realName = "Coppet-Gare";
        }
        else if (ArgStopName.equalsIgnoreCase("Hôpital Trois-Chêne"))
        {
            realName = "Hôpital 3-Chêne";
        }
        else if (ArgStopName.equalsIgnoreCase("Hôpital 3-Chêne"))
        {
            realName = "Hôpital Trois-Chêne";
        }
        else if (ArgStopName.equalsIgnoreCase("C.O.Renard"))
        {
            realName = "CO Renard";
        }
        else if (ArgStopName.equalsIgnoreCase("Hermance-village"))
        {
            realName = "Hermance";
        }
        else if (ArgStopName.equalsIgnoreCase("P+R Veigy"))
        {
            realName = "Veigy-Douane";
        }
        else if (ArgStopName.equalsIgnoreCase("Beaumont"))
        {
            realName = "Beaumont Chable";
        }
        else if (ArgStopName.equalsIgnoreCase("Athenaz"))
        {
            realName = "Athenaz-Ecole";
        }
        else if (ArgStopName.equalsIgnoreCase("Chavanne-bois") || ArgStopName.equalsIgnoreCase("Chavannes-des-Bois"))
        {
            realName = "Chavannes des B.";
        }


        return realName;
    }

    public static String getCode(final String ArgName) {
        String code = ArgName;

        if (ArgName.equalsIgnoreCase("Jardin Botanique")) {
            code = "Jar. Botanique";
        } else if (ArgName.equalsIgnoreCase("Vernier-Village")) {
            code = "Vernier";
        } else if (ArgName.equalsIgnoreCase("Jussy-Meurets")) {
            code = "Jussy";
        } else if (ArgName.equalsIgnoreCase("Ferney-V.-Mairie")) {
            code = "Ferney-Voltaire";
        } else if (ArgName.equalsIgnoreCase("CS Bécassière")) {
            code = "CS LA BECASSIERE";
        } else if (ArgName.equalsIgnoreCase("Th-Vallard-Dne")) {
            code = "Thônex-Vallard";
        } else if (ArgName.equalsIgnoreCase("Veyr-Tournettes")) {
            code = "Veyrier-tournet.";
        } else if (ArgName.equalsIgnoreCase("Valleiry")) {
            code = "VALLEIRY CH-LIEU";
        } else if (ArgName.equalsIgnoreCase("Carouge-Rondeau")) {
            code = "CAROUGE";
        } else if (ArgName.equalsIgnoreCase("Crêts-de-Champel")) {
            code = "Champel";
        } else if (ArgName.equalsIgnoreCase("Cr.-de-Rozon-Dne")) {
            code = "Croix-de-Rozon";
        } else if (ArgName.equalsIgnoreCase("Gy-Temple")) {
            code = "Gy";
        } else if (ArgName.equalsIgnoreCase("Corsinge-Village")) {
            code = "Gy - Corsinge";
        } else if (ArgName.equalsIgnoreCase("Ste-Clotilde")) {
            code = "Sainte-Clotilde";
        } else if (ArgName.equalsIgnoreCase("Hôpital-La Tour")) {
            code = "Hôpital La Tour";
        } else if (ArgName.equalsIgnoreCase("Collonges-bourg")) {
            code = "Bourg d'en haut";
        } else if (ArgName.equalsIgnoreCase("C.O. Renard")) {
            code = "c.o.renard";
        } else if (ArgName.equalsIgnoreCase("P+R Challex-Hal.")) {
            code = "CHALLEX-LA HALLE";
        } else if (ArgName.equalsIgnoreCase("Challex-La-Halle")) {
            code = "CHALLEX-LA HALLE";
        } else if (ArgName.equalsIgnoreCase("Seymaz")) {
            code = "C.o. seymaz";
        } else if (ArgName.equalsIgnoreCase("Lycée Internat.")) {
            code = "L. international";
        } else if (ArgName.equalsIgnoreCase("Z.I. Les Moulins")) {
            code = "Zi les moulins";
        } else if (ArgName.equalsIgnoreCase("Bachet-de-Pesay")) {
            code = "Bachet";
        } else if (ArgName.equalsIgnoreCase("La Plaine-Ecole")) {
            code = "La plaine";
        } else if (ArgName.equalsIgnoreCase("Vitam'Parc")) {
            code = "Neydens-Vitam";
        } else if (ArgName.equalsIgnoreCase("Avenue du Jura")) {
            code = "Ferney av. jura";
        } else if (ArgName.equalsIgnoreCase("Veyrier-Douane")) {
            code = "Veyrier";
        } else if (ArgName.equalsIgnoreCase("Thoiry-Mairie")) {
            code = "Thoiry";
        } else if (ArgName.equalsIgnoreCase("Pl. Eaux-Vives")) {
            code = "Pl.Eaux-Vives";
        } else if (ArgName.equalsIgnoreCase("Stand")) {
            code = "Bel-Air - stand";
        } else if (ArgName.equalsIgnoreCase("Jar.-Botanique")) {
            code = "Jar. botanique";
        } else if (ArgName.equalsIgnoreCase("Veigy-Village")) {
            code = "P+R VEIGY";
        } else if (ArgName.equalsIgnoreCase("Gex-Aiglette")) {
            code = "GEX";
        } else if (ArgName.equalsIgnoreCase("Puplinge-Mairie")) {
            code = "PUPLINGE-MAIRIE";
        } else if (ArgName.equalsIgnoreCase("Vallet")) {
            code = "ONEX";
        } else if (ArgName.equalsIgnoreCase("Russin-Village")) {
            code = "RUSSIN";
        } else if (ArgName.equalsIgnoreCase("Augustins")) {
            code = "HOPITAUX";
        } else if (ArgName.equalsIgnoreCase("Armes")) {
            code = "Palettes - armes";
        } else if (ArgName.equalsIgnoreCase("Bachet-de-Pesay")) {
            code = "Palettes bachet";
        } else if (ArgName.equalsIgnoreCase("Coppet-Gare")) {
            code = "COPPET";
        } else if (ArgName.equalsIgnoreCase("La Plaine-Gare")) {
            code = "La Plaine";
        }
        else if (ArgName.equalsIgnoreCase("Hôpital Trois-Chêne") || ArgName.equalsIgnoreCase("Hôpital 3-Chêne") || ArgName.equalsIgnoreCase("HOPITAL 3-CHENE"))
        {
            code = "H. TROIS-CHENE";
        }
        else if (ArgName.equalsIgnoreCase("C.O.Renard"))
        {
            code = "CO RENARD";
        }
        else if (ArgName.equalsIgnoreCase("Hermance-village"))
        {
            code = "hermance";
        }
        else if (ArgName.equalsIgnoreCase("P+R VEIGY"))
        {
            code = "Veigy-douane";
        }
        else if (ArgName.equalsIgnoreCase("Beaumont Chable"))
        {
            code = "Beaumont";
        }
        else if (ArgName.equalsIgnoreCase("Athenaz-Ecole"))
        {
            code = "Athenaz";
        }
        else if (ArgName.equalsIgnoreCase("Chavannes des B."))
        {
            code = "Chavannes-bois";
        }

        code = Normalizer.normalize(code, Normalizer.Form.NFD);
        code = code.replaceAll("[^\\p{ASCII}]", "");
        code = code.toUpperCase(Locale.FRENCH);

        return code;
    }

    public static void manageVisibility(Stop ArgStop) {
        final String Name = ArgStop.getName();

        List<String> namesInvisible = new ArrayList<String>();
        namesInvisible.add("Bus scolaire");
        namesInvisible.add("Hopitaux");

        for (String name : namesInvisible) {
            if (name.equalsIgnoreCase(Name)) {
                ArgStop.setVisible(false);
                break;
            }
        }
    }

    public static List<Stop> sortStopsByDistance(List<Stop> ArgStops, final Location ArgLoc) {
        List<Stop> stops = new ArrayList<Stop>();
        List<PhysicalStop> physicalStops = new ArrayList<PhysicalStop>();

        List<Long> physicalStopIds = new ArrayList<Long>();
        for (Stop stop : ArgStops) {
            for (PhysicalStop physicalStop : stop.getPhysicalStops()) {
                if (!physicalStopIds.contains(physicalStop.getId())) {
                    physicalStops.add(physicalStop);
                    physicalStopIds.add(physicalStop.getId());
                }
            }
        }

        StopTools.sortByDistance(physicalStops, ArgLoc);

       /* Collections.sort(physicalStops, new Comparator<PhysicalStop>() {
            public int compare(PhysicalStop physicalStop, PhysicalStop physicalStop2) {
                float distance1 = ArgLoc.distanceTo(physicalStop.getLocation());
                float distance2 = ArgLoc.distanceTo(physicalStop2.getLocation());

                if (distance1 < distance2) {
                    return -1;
                } else if (distance1 == distance2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });*/

        List<Long> stopIds = new ArrayList<Long>();
        for (PhysicalStop physicalStop : physicalStops) {
            for (Stop stop : ArgStops) {
                if (stop.getId() == physicalStop.getStopId() && !stopIds.contains(stop.getId())) {
                    stopIds.add(stop.getId());
                    stops.add(stop);
                    break;
                }
            }
        }

        return stops;
    }

    public static void sortByDistance(List<? extends EntityWithNameAndLocation> ArgEWNL, final Location ArgLoc) {
        Collections.sort(ArgEWNL, new Comparator<EntityWithNameAndLocation>() {
            public int compare(EntityWithNameAndLocation ewnl, EntityWithNameAndLocation ewnl2) {
                float distance1 = ArgLoc.distanceTo(ewnl.getLocation());
                float distance2 = ArgLoc.distanceTo(ewnl2.getLocation());

                if (distance1 < distance2) {
                    return -1;
                } else if (distance1 == distance2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });


    }

    public static void sortConnections(List<Line> ArgConnections) {
        Collections.sort(ArgConnections, new Comparator<Line>() {

            @Override
            public int compare(Line lhs, Line rhs) {
                int result;

                String firstName = lhs.getName();
                String secondName = rhs.getName();

                if (firstName.length() == 1 && Character.isDigit(firstName.charAt(0))) {
                    firstName = "0" + firstName;
                }

                if (secondName.length() == 1 && Character.isDigit(secondName.charAt(0))) {
                    secondName = "0" + secondName;
                }

                result = firstName.compareTo(secondName);


                return result;
            }
        });
    }

    public static void sortStopsByDistance(Location argLoc, SortTools.FilterDistanceType argFilterDistanceType, int argNumber, List<Stop> argStops) {
        SortTools.sortStopsByDistance(argLoc, argFilterDistanceType, argStops);

    }
}