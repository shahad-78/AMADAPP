package com.example.amadapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.amadapp.Model.Challenge;
import com.example.amadapp.Model.EducationContent;
import com.example.amadapp.Model.Tree;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class LauncherActivity extends AppCompatActivity {

    private List<EducationContent> educationContents;
    private List<Challenge> challengeList;
    private List<Tree> treeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //educationContents = new ArrayList<>();
        //writeConetnt();
        //writeChallenges();
       // writeTrees();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LauncherActivity.this, MenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

    }

    private void writeTrees() {
        treeList =new ArrayList<>();
        treeList.add(new Tree("Talh (Acacia)",
                "Acacia gerrardii",
                "The majestic umbrella-shaped tree native to the Najd plateau. It is extremely drought-tolerant and provides essential shade in the central desert.",
                "Central",
                "https://www.mshtly.com/uploads/products/%D8%B4%D8%AC%D8%B1%D8%A9-%D8%A7%D9%84%D8%B7%D9%84%D8%AD-%D8%A7%D9%84%D8%A7%D9%81%D8%B1%D9%8A%D9%82%D9%89.jpg"));

        treeList.add(new Tree("Sidr (Lote Tree)",
                "Ziziphus spina-christi",
                "A culturally significant tree mentioned in the Quran. It is robust, heat-tolerant, and produces the sweet Nabq fruit. Suitable for almost all Saudi environments.",
                "All",
                "https://loteandco.com/cdn/shop/articles/Sidr_Tree.jpg?v=1550323523&width=1780"));

        treeList.add(new Tree("Ghaf Tree",
                "Prosopis cineraria",
                "A resilient tree native to Eastern Arabia. It survives on very little water, stabilizes sand dunes, and stays green even in harsh summers.",
                "Eastern",
                "https://5.imimg.com/data5/SELLER/Default/2025/5/511025789/YE/HL/CN/12382063/prosopis-cineraria-vanni-tree-seed-1000x1000.jpg"));

        treeList.add(new Tree("Arar (Juniper)",
                "Juniperus procera",
                "Found in the high-altitude mountains of the Sarawat range (Hijaz/Asir). It thrives in cooler, misty environments and prevents soil erosion.",
                "Western",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/8/81/Juniperus_procera_cones.JPG/1200px-Juniperus_procera_cones.JPG"));

        treeList.add(new Tree("Samr",
                "Acacia tortilis",
                "Common in the foothills and plains of the Western region. It is a key source of nectar for the famous Saudi mountain honey.",
                "Western",
                "https://www.aldarmakyuae.com/wp-content/uploads/2021/11/OIP-1-1.jpg"));

        treeList.add(new Tree("Qurm (Mangrove)",
                "Avicennia marina",
                "Grows in the coastal salt waters of the Eastern Province (e.g., Tarout Bay). Essential for marine biodiversity and carbon sequestration.",
                "Eastern",
                "https://upload.wikimedia.org/wikipedia/commons/f/f1/Avic_marin_070728_030_mank_rsz.jpg?20070814021142"));

        treeList.add(new Tree("Sarah (Maerua)",
                "Maerua crassifolia",
                "A dense, evergreen tree found in Central Arabia. It is known for its ability to stay green during severe droughts.",
                "Central",
                "https://upload.wikimedia.org/wikipedia/commons/3/3e/Maerua_crassifolia.jpg"));

        treeList.add(new Tree("Date Palm",
                "Phoenix dactylifera",
                "The icon of Saudi agriculture. While it requires irrigation, it is perfectly adapted to the heat of the entire Kingdom.",
                "All",
                "https://ethnoplants.com/3674-large_default/phoenix-dactylifera-date-palm-seeds.jpg"));
        FirebaseDatabase database = FirebaseDatabase.getInstance();


        DatabaseReference placesRef = database.getReference("Trees");


        // Iterate over the list and save each Place object as a new child
        for (Tree tree : treeList) {

            // The 'push()' method creates a unique key for each new child node
            placesRef.push().setValue(tree)
                    .addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                    });
        }

    }

    private void writeChallenges() {
        challengeList = new ArrayList<>();
        challengeList.add(new Challenge(
                "School Green Campus Initiative",
                "Plant trees in school campuses to create educational green spaces and improve learning environments.",
                "2025-12-15",
                "2025-12-30",
                180,
                "https://www.ugaoo.com/cdn/shop/articles/high-angle-kids-getting-ready-plant-clover.jpg?v=1686917045&width=1780"
        ));

        challengeList.add(new Challenge(
                "Water-Smart Planting Challenge",
                "Plant drought-resistant native species that require minimal irrigation. Help conserve water resources.",
                "2025-02-01",
                "2025-04-30",
                150,
                "https://permaculturepractice.com/wp-content/uploads/2024/10/Drought-resistant-plants.jpg"
                )
        );

        challengeList.add(new Challenge(
                "Spring Planting Challenge",
                "Plant 10 trees during the spring season. Help increase green coverage and combat desertification.",
                "2026-03-01",
                "2026-05-31",
                200,
                "https://saudipedia.com/en/saudipediaen/uploads/images/2024/07/10/thumbs/600x600/67895.jpg"
                )
        );

        challengeList.add(new Challenge(
                "Water Conservation Hero",
                "Reduce your water consumption by 20% for one month. Track and report your water-saving activities.",
                "2025-12-01",
                "2025-12-31",
                150,
                "https://www.cleanlink.com/resources/editorial/2021/earth-27487-sstock.jpg")
        );

        challengeList.add(new Challenge(
                "Environmental Watchdog",
                "Report 5 degraded areas or environmental hazards in your community using the AMAD reporting system.",
                "2025-01-01",
                "2025-12-31",
                200,
                "https://i0.wp.com/www.ecomena.org/wp-content/uploads/2025/05/land-degradation-middle-east.jpg?ssl=1")
        );
        challengeList.add(new Challenge(
                "Recycling Champion",
                "Collect and properly recycle 10kg of recyclable materials (paper, plastic, metal).",
                "2025-05-01",
                "2025-08-31",
                200,
                "https://www.recyclefromhome.com/wp-content/uploads/2023/01/sorted-recycling-bins-1536x909.jpg")
        );

        challengeList.add(new Challenge(
                "Energy Saver",
                "Reduce your electricity consumption by 15% for two months through efficiency measures.",
                "2026-01-01",
                "2026-01-31",
                220,
                "https://www.estiasynergie.com/wp-content/uploads/2021/02/economiser-de-lenergie-estiasynergie-e1614237840812.png")
        );
        FirebaseDatabase database = FirebaseDatabase.getInstance();


        DatabaseReference placesRef = database.getReference("Challenges");


        // Iterate over the list and save each Place object as a new child
        for (Challenge challenge : challengeList) {

            // The 'push()' method creates a unique key for each new child node
            placesRef.push().setValue(challenge)
                    .addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }

    private void writeConetnt() {

        educationContents.add(new EducationContent("Environmental sustainability",
                "Environmental sustainability is key to survival for us and the planet. We are going to share some examples in order to spread awareness and encourage our readers to adopt sustainable best practices, but before we do, let’s take a look at what environmental sustainability is.\n" +
                        "\n" +
                        "What is Environmental Sustainability?\n" +
                        "We take quite a lot from the environment – including water, space to grow our crops, harvest wood, produce energy, and the list goes on – but we rarely give back in return. Environmental sustainability is the practice of responsibly managing natural resources so that they are still available for future generations. In a nutshell, it’s taking care of the environment so that the gesture is reciprocated.\n" +
                        "\n" +
                        "Why is Environmental Sustainability Important?\n" +
                        "When not taken care of, the environment fires back. Deforestation, extinction, over-fishing, global warming, not to mention water waste, are all examples of what happens when we abuse our resources. Environmentally sustainable practices are key to survival for generations to come. Here are some examples of effective measures you can start taking.\n" +
                        "\n" +
                        "Environmental Sustainability Examples\n" +
                        "There are a number of ways to practice environmental sustainability, both on a corporate and individual level. A few examples include:\n" +
                        "\n" +
                        "Recycling – Recycle plastic, metals, and minerals. Recycling metals and minerals while mining can help avoid environmental damage.\n" +
                        "Renewable energy – Unlike non-renewable energy, it doesn’t get depleted and it results in significantly less pollution.\n" +
                        "Crop rotation – Maintain the quality of soil and increase crop yield with crop rotation.\n" +
                        "Environmental Sustainability at Home\n" +
                        "While some of these examples may seem a bit far-fetched, there are ways to help protect the planet from the comfort of your own home.\n" +
                        "\n" +
                        "Turn off the Water When Not in Use\n" +
                        "Did you know you can save up to 8 gallons of water each day, just by turning off the faucet when brushing your teeth or shaving? This is the easiest way to save water, yet the most often overlooked.\n" +
                        "\n" +
                        "Save Energy\n" +
                        "Help the planet by saving energy and reducing carbon emissions. The easiest way to do this is by switching off appliances when they’re not in use. Just because they’re not in use, doesn’t mean they’re not using energy.\n" +
                        "\n" +
                        "Wash Laundry in Full Loads\n" +
                        "A cycle in the washing machine can use about 20 gallons of water, not to mention all the energy it consumes. Wait until you have a full load to wash the laundry so you can easily contribute to saving energy, water, and the planet.\n" +
                        "\n" +
                        "Install Tap Aerators\n" +
                        "Installing tap aerators on your faucets removes the dirt from your water and slows the flow so less water is wasted. Achieve up to 98% saving with tap aerators.\n" +
                        "\n" +
                        "Environmentally sustainable practices are key to survival for generations to come, and it’s essential that we properly make use of the resources we have. At Pure Blue Sustainability, the leading supplier of water-saving solutions in the UAE, you can find a variety of tap nozzles, showerheads, and hoses for your home or business. Installing them can help save water, energy, and the planet, one day at a time.",
                "2025-12-01",
                "https://purebluesustainability.com/ar/environmental-sustainability-examples-tips-to-implement-them/",
                "https://purebluesustainability.com/wp-content/uploads/2022/02/Environmental-Sustainability-Examples-Tips-to-Implement-Them.jpg"));

        educationContents.add(new EducationContent("Best practices for tree planting",
                "Tree planting is more than just putting a sapling in the ground; it’s a process that demands substantial planning and knowledge. Utilizing the right tree planting techniques ensures not only the survival but also the thriving of the tree, which in turn contributes positively to environmental quality and property value. But planting mistakes can lead to greater maintenance issues or hazards, particularly near utilities or other infrastructure.\n" +
                        "\n" +
                        "Lindsey Purcell, an Urban Forestry Specialist, underscores the importance of knowing the mature size of a tree before selecting a planting site. This foresight can help maintain low maintenance needs and optimize space usage. For example, it is recommended to plant trees at least 20 feet from the house, extending this distance to 40 feet for larger shade trees to accommodate their growth properly.\n" +
                        "\n" +
                        "Another crucial aspect involves choosing species less common in your locality to avoid the overuse of certain types. Diversifying your tree selection helps in mitigating the risk of pests and diseases. When we think about soil preparation, the recommended approach is to loosen and aerate the planting area to a diameter approximately three times that of the root ball. Proper soil preparation can significantly enhance the establishment and longevity of the tree.\n" +
                        "\n" +
                        "Planting trees during early fall, especially in Indiana, is advised to allow optimal root establishment before winter sets in. Consistent watering is critical during the establishment period of up to two years, ensuring at least 1 inch of water per week. Researchers like Dr. Bonnie Appleton and Dr. Alex Shigo have contributed to refining tree planting techniques, advocating for practices that support better root development and soil interaction.\n" +
                        "\n" +
                        "Understanding that larger trees, such as those with a 2-inch caliper or more, might require more frequent watering beyond the first growing season can prevent premature tree loss. Additionally, be mindful of local regulations which might require permits, especially when planting on public property. This comprehensive approach to tree planting techniques and soil preparation lays a robust foundation for successful and sustainable tree growth.",
                "2025-12-01",
                "https://buckstreeservice.ca/tree-planting-best-practices-a-comprehensive-guide/",
                "https://www.aesthetictree.ca/wp-content/uploads/2024/10/Successful-Tree-Care-Start-with-Proper-Tree-Planting-1.jpg"));

        educationContents.add(new EducationContent("Desertification: impacts and solutions",
                "Desertification affects vital parts of our lives, such as food security and biodiversity. Let’s discuss the effects of desertification and the best way to address this growing issue. \n" +
                        "\n" +
                        "What is desertification?\n" +
                        "According to the United Nations, we lose 12 million hectares of fertile land each year to desertification. This statistic is alarming, considering how healthy soil directly influences our livelihoods.\n" +
                        "\n" +
                        "Desertification is when fertile land turns into a desert or semi-arid area. This happens due to a combination of natural and human-induced factors, such as drought, deforestation, overgrazing, unsustainable agricultural practices, and changing environmental conditions. Desertification significantly impacts the environment, human health, and economic development, particularly in arid and semi-arid regions.",
                "2025-12-01",
                "https://www.green.earth/desertification",
                "https://ccafs.cgiar.org/sites/default/files/styles/image_full/public/blog/pictures/4163567741_bd0ecd3be9_z1.jpg?itok=9OLjHYzV"));

        FirebaseDatabase database = FirebaseDatabase.getInstance();


        DatabaseReference placesRef = database.getReference("EducationalContents");


        // Iterate over the list and save each Place object as a new child
        for (EducationContent content : educationContents) {

            // The 'push()' method creates a unique key for each new child node
            placesRef.push().setValue(content)
                    .addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                    });
        }

    }
}