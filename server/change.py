import re

# Sample new list of materials (partial for demonstration)
new_list = """
ITEM("item", Item.class, 1),
    EXPERIENCE_ORB("experience_orb", ExperienceOrb.class, 2),
    AREA_EFFECT_CLOUD("area_effect_cloud", AreaEffectCloud.class, 3),
    ELDER_GUARDIAN("elder_guardian", ElderGuardian.class, 4),
    WITHER_SKELETON("wither_skeleton", WitherSkeleton.class, 5),
    STRAY("stray", Stray.class, 6),
    EGG("egg", Egg.class, 7),
    LEASH_KNOT("leash_knot", LeashHitch.class, 8),
    PAINTING("painting", Painting.class, 9),
    ARROW("arrow", Arrow.class, 10),
    SNOWBALL("snowball", Snowball.class, 11),
    FIREBALL("fireball", LargeFireball.class, 12),
    SMALL_FIREBALL("small_fireball", SmallFireball.class, 13),
    ENDER_PEARL("ender_pearl", EnderPearl.class, 14),
    EYE_OF_ENDER("eye_of_ender", EnderSignal.class, 15),
    POTION("potion", ThrownPotion.class, 16),
    EXPERIENCE_BOTTLE("experience_bottle", ThrownExpBottle.class, 17),
    ITEM_FRAME("item_frame", ItemFrame.class, 18),
    WITHER_SKULL("wither_skull", WitherSkull.class, 19),
    TNT("tnt", TNTPrimed.class, 20),
    FALLING_BLOCK("falling_block", FallingBlock.class, 21),
    FIREWORK_ROCKET("firework_rocket", Firework.class, 22),
    HUSK("husk", Husk.class, 23),
    SPECTRAL_ARROW("spectral_arrow", SpectralArrow.class, 24),
    SHULKER_BULLET("shulker_bullet", ShulkerBullet.class, 25),
    DRAGON_FIREBALL("dragon_fireball", DragonFireball.class, 26),
    ZOMBIE_VILLAGER("zombie_villager", ZombieVillager.class, 27),
    SKELETON_HORSE("skeleton_horse", SkeletonHorse.class, 28),
    ZOMBIE_HORSE("zombie_horse", ZombieHorse.class, 29),
    ARMOR_STAND("armor_stand", ArmorStand.class, 30),
    DONKEY("donkey", Donkey.class, 31),
    MULE("mule", Mule.class, 32),
    EVOKER_FANGS("evoker_fangs", EvokerFangs.class, 33),
    EVOKER("evoker", Evoker.class, 34),
    VEX("vex", Vex.class, 35),
    VINDICATOR("vindicator", Vindicator.class, 36),
    ILLUSIONER("illusioner", Illusioner.class, 37),
    COMMAND_BLOCK_MINECART("command_block_minecart", CommandMinecart.class, 40),
    BOAT("boat", Boat.class, 41),
    MINECART("minecart", RideableMinecart.class, 42),
    CHEST_MINECART("chest_minecart", StorageMinecart.class, 43),
    FURNACE_MINECART("furnace_minecart", PoweredMinecart.class, 44),
    TNT_MINECART("tnt_minecart", ExplosiveMinecart.class, 45),
    HOPPER_MINECART("hopper_minecart", HopperMinecart.class, 46),
    SPAWNER_MINECART("spawner_minecart", SpawnerMinecart.class, 47),
    CREEPER("creeper", Creeper.class, 50),
    SKELETON("skeleton", Skeleton.class, 51),
    SPIDER("spider", Spider.class, 52),
    GIANT("giant", Giant.class, 53),
    ZOMBIE("zombie", Zombie.class, 54),
    SLIME("slime", Slime.class, 55),
    GHAST("ghast", Ghast.class, 56),
    ZOMBIFIED_PIGLIN("zombified_piglin", PigZombie.class, 57),
    ENDERMAN("enderman", Enderman.class, 58),
    CAVE_SPIDER("cave_spider", CaveSpider.class, 59),
    SILVERFISH("silverfish", Silverfish.class, 60),
    BLAZE("blaze", Blaze.class, 61),
    MAGMA_CUBE("magma_cube", MagmaCube.class, 62),
    ENDER_DRAGON("ender_dragon", EnderDragon.class, 63),
    WITHER("wither", Wither.class, 64),
    BAT("bat", Bat.class, 65),
    WITCH("witch", Witch.class, 66),
    ENDERMITE("endermite", Endermite.class, 67),
    GUARDIAN("guardian", Guardian.class, 68),
    SHULKER("shulker", Shulker.class, 69),
    PIG("pig", Pig.class, 90),
    SHEEP("sheep", Sheep.class, 91),
    COW("cow", Cow.class, 92),
    CHICKEN("chicken", Chicken.class, 93),
    SQUID("squid", Squid.class, 94),
    WOLF("wolf", Wolf.class, 95),
    MOOSHROOM("mooshroom", MushroomCow.class, 96),
    SNOW_GOLEM("snow_golem", Snowman.class, 97),
    OCELOT("ocelot", Ocelot.class, 98),
    IRON_GOLEM("iron_golem", IronGolem.class, 99),
    HORSE("horse", Horse.class, 100),
    RABBIT("rabbit", Rabbit.class, 101),
    POLAR_BEAR("polar_bear", PolarBear.class, 102),
    LLAMA("llama", Llama.class, 103),
    LLAMA_SPIT("llama_spit", LlamaSpit.class, 104),
    PARROT("parrot", Parrot.class, 105),
    VILLAGER("villager", Villager.class, 120),
    END_CRYSTAL("end_crystal", EnderCrystal.class, 200),
    TURTLE("turtle", Turtle.class, -1),
    PHANTOM("phantom", Phantom.class, -1),
    TRIDENT("trident", Trident.class, -1),
    COD("cod", Cod.class, -1),
    SALMON("salmon", Salmon.class, -1),
    PUFFERFISH("pufferfish", PufferFish.class, -1),
    TROPICAL_FISH("tropical_fish", TropicalFish.class, -1),
    DROWNED("drowned", Drowned.class, -1),
    DOLPHIN("dolphin", Dolphin.class, -1),
    CAT("cat", Cat.class, -1),
    PANDA("panda", Panda.class, -1),
    PILLAGER("pillager", Pillager.class, -1),
    RAVAGER("ravager", Ravager.class, -1),
    TRADER_LLAMA("trader_llama", TraderLlama.class, -1),
    WANDERING_TRADER("wandering_trader", WanderingTrader.class, -1),
    FOX("fox", Fox.class, -1),
    BEE("bee", Bee.class, -1),
    HOGLIN("hoglin", Hoglin.class, -1),
    PIGLIN("piglin", Piglin.class, -1),
    STRIDER("strider", Strider.class, -1),
    ZOGLIN("zoglin", Zoglin.class, -1),
    PIGLIN_BRUTE("piglin_brute", PiglinBrute.class, -1),
    AXOLOTL("axolotl", Axolotl.class, -1),
    GLOW_ITEM_FRAME("glow_item_frame", GlowItemFrame.class, -1),
    GLOW_SQUID("glow_squid", GlowSquid.class, -1),
    GOAT("goat", Goat.class, -1),
    MARKER("marker", Marker.class, -1),
    ALLAY("allay", Allay.class, -1),
    CHEST_BOAT("chest_boat", ChestBoat.class, -1),
    FROG("frog", Frog.class, -1),
    TADPOLE("tadpole", Tadpole.class, -1),
    WARDEN("warden", Warden.class, -1),
    CAMEL("camel", Camel.class, -1),
    BLOCK_DISPLAY("block_display", BlockDisplay.class, -1),
    INTERACTION("interaction", Interaction.class, -1),
    ITEM_DISPLAY("item_display", ItemDisplay.class, -1),
    SNIFFER("sniffer", Sniffer.class, -1),
    TEXT_DISPLAY("text_display", TextDisplay.class, -1),
    BREEZE("breeze", Breeze.class, -1),
    WIND_CHARGE("wind_charge", WindCharge.class, -1),
    BREEZE_WIND_CHARGE("breeze_wind_charge", BreezeWindCharge.class, -1),
    ARMADILLO("armadillo", Armadillo.class, -1),
    BOGGED("bogged", Bogged.class, -1),
    OMINOUS_ITEM_SPAWNER("ominous_item_spawner", OminousItemSpawner.class, -1),
    FISHING_BOBBER("fishing_bobber", FishHook.class, -1, false),
    LIGHTNING_BOLT("lightning_bolt", LightningStrike.class, -1),
    PLAYER("player", Player.class, -1, false),
    UNKNOWN((String)null, (Class)null, -1, false);
"""

# Function to parse the new material list, ignoring decorators and comments
def parse_new_list(new_list):
    materials = []
    # Match lines like MATERIAL_NAME(ID1, ID2, Class) and capture only the name
    pattern = re.compile(r'([A-Z_]+)\(')
    matches = pattern.findall(new_list)
    for match in matches:
        materials.append(match)
    return materials

# Function to convert the parsed materials to the enum format
def convert_to_enum(materials):
    enum_output = "enum MinecraftMaterial {\n"
    for i, material in enumerate(materials):
        enum_output += f"    {material} = {i};\n"
    enum_output += "}\n"
    return enum_output

# Parse the new material list
parsed_materials = parse_new_list(new_list)

# Convert to enum format
enum_format = convert_to_enum(parsed_materials)

# Output the result
print(enum_format)
