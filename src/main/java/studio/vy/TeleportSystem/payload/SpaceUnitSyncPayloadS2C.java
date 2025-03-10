//package studio.vy.TeleportSystem.payload;
//
//import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//import net.minecraft.network.PacketByteBuf;
//import net.minecraft.network.codec.PacketCodec;
//import net.minecraft.network.packet.CustomPayload;
//import net.minecraft.server.network.ServerPlayerEntity;
//import studio.vy.TeleportSystem.SpaceUnit;
//import studio.vy.TeleportSystem.screen.UnitList;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public record SpaceUnitSyncPayloadS2C(List<SpaceUnit> units) implements CustomPayload {
//    public static final Id<SpaceUnitSyncPayloadS2C> ID = CustomPayload.id("space_unit_sync");
//    public static final PacketCodec<PacketByteBuf, SpaceUnitSyncPayloadS2C> CODEC = PacketCodec.tuple(
//            new PacketCodec<>() {
//                public void encode(PacketByteBuf buf, List<SpaceUnit> units) {
//                    buf.writeCollection(units, SpaceUnit.PACKET_CODEC);
//                }
//                public List<SpaceUnit> decode(PacketByteBuf buf) {
//                    return buf.readCollection(ArrayList::new, SpaceUnit.PACKET_CODEC);
//                }
//            },
//            SpaceUnitSyncPayloadS2C::units,
//            SpaceUnitSyncPayloadS2C::new
//    );
//
//    @Override
//    public Id<? extends CustomPayload> getId() {
//        return ID;
//    }
//
//    public static void send(ServerPlayerEntity player, List<SpaceUnit> units) {
//        ServerPlayNetworking.send(player, new SpaceUnitSyncPayloadS2C(units));
//    }
//
//    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<SpaceUnitSyncPayloadS2C> {
//        @Override
//        public void receive(SpaceUnitSyncPayloadS2C payload, ClientPlayNetworking.Context context) {
//            if (context.client().currentScreen instanceof UnitList) {
//                UnitList unitList = (UnitList) context.client().currentScreen;
//                if (payload.units!=null) {
//                    unitList.refresh(payload.units);
//                }
//                else {
//                    List<SpaceUnit> unitss = new ArrayList<>();
//                    unitss.add(new SpaceUnit("home",0,0,0,"minecraft:overworld",context.client().player.getUuid()));
//                    unitList.refresh(unitss);
//                }
//            }
//        }
//    }
//}
