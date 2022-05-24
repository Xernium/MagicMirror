package me.fivepb.magicmirror;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("magicmirror")
public class MagicMirror {

    private static MagicMirror instance;

    public static MagicMirror getInstance() {
        return instance;
    }

    private List<QuestionResponsePair> comm = new ArrayList<>();

    public void track(ClientboundCustomQueryPacket question) {
        comm.add(new QuestionResponsePair(question.getIdentifier().toString(), readBytesZeroIndex(question.getData()), question.getTransactionId()));
    }

    public void track(ServerboundCustomQueryPacket response) {
        QuestionResponsePair match = null;
        for(QuestionResponsePair p : ImmutableList.copyOf(comm)) {
            if (response.getTransactionId() == p.getTransaction()) {
                match = p;
                break;
            }
        }
        if (match == null) {
            throw new IllegalArgumentException("Invalid transaction");
        }
        if (response.getData() != null) {
            match.setAnswer(readBytesZeroIndex(response.getData()));
        }
    }


    public void clearQueue() {
        comm.clear();
    }

    public void onDone(){

        JsonArray jsa = new JsonArray();
        final List<QuestionResponsePair> comm = ImmutableList.copyOf(this.comm);
        clearQueue();

        for(int i = 0; i < comm.size(); i++) {
            JsonObject oj = new JsonObject();
            oj.addProperty("index", i);
            QuestionResponsePair q = comm.get(i);
            oj.addProperty("identifier", q.getIdentifier());
            oj.addProperty("question", Base64.getEncoder().encodeToString(q.getQuestion()));
            oj.addProperty("answer", Base64.getEncoder().encodeToString(q.getAnswer()));
            jsa.add(oj);
        }

        String workingDirectory = System.getProperty("user.dir");
        File outFile = new File(workingDirectory, "dump-server-" + Instant.now().toString() + ".json");
        try (FileWriter fw = new FileWriter(outFile)){
            fw.write(jsa.toString());
            sendChat("Written handshake to " + outFile.getPath() + " successfully.");
        } catch (IOException ignored) {
            sendChat("Couldn't write to " + outFile.getPath());
        }
    }

    public void sendChat(String message) {
        Minecraft.getInstance().gui.handleChat(ChatType.SYSTEM, new TextComponent(message), new UUID(0,0));
    }
    public static byte[] readBytesZeroIndex(FriendlyByteBuf buf){
        final int marker = buf.readerIndex();
        byte[] data = new byte[buf.readableBytes()];
        // Fucking wrapper
        for (int i = 0; i < data.length; i++) {
            data[i] = buf.readByte();
        }
        buf.readerIndex(marker);
        return data;
    }

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public MagicMirror() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        instance = this;
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("magicmirror", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.messageSupplier().get()).
                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}
