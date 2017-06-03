/*
 * Copyright (c) 2016 Matsv
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.matsv.viabackwards.protocol.protocol1_10to1_11.packets;

import nl.matsv.viabackwards.api.rewriters.BlockItemRewriter;
import nl.matsv.viabackwards.protocol.protocol1_10to1_11.EntityTypeNames;
import nl.matsv.viabackwards.protocol.protocol1_10to1_11.Protocol1_10To1_11;
import nl.matsv.viabackwards.utils.Block;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_9;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.chunks.Chunk1_9_3_4;
import us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.types.Chunk1_9_3_4Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import us.myles.viaversion.libs.opennbt.tag.builtin.StringTag;

public class BlockItemPackets extends BlockItemRewriter<Protocol1_10To1_11> {
    @Override
    protected void registerPackets(Protocol1_10To1_11 protocol) {
        /* Item packets */

        // Set slot packet
        protocol.registerOutgoing(State.PLAY, 0x16, 0x16, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot ID
                map(Type.ITEM); // 2 - Slot Value

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item stack = wrapper.get(Type.ITEM, 0);
                        wrapper.set(Type.ITEM, 0, handleItemToClient(stack));
                    }
                });
            }
        });

        // Window items packet
        protocol.registerOutgoing(State.PLAY, 0x14, 0x14, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.ITEM_ARRAY); // 1 - Window Values

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item[] stacks = wrapper.get(Type.ITEM_ARRAY, 0);
                        for (int i = 0; i < stacks.length; i++)
                            stacks[i] = handleItemToClient(stacks[i]);
                    }
                });
            }
        });

        // Entity Equipment Packet
        protocol.registerOutgoing(State.PLAY, 0x3C, 0x3C, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.VAR_INT); // 1 - Slot ID
                map(Type.ITEM); // 2 - Item

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item stack = wrapper.get(Type.ITEM, 0);
                        wrapper.set(Type.ITEM, 0, handleItemToClient(stack));
                    }
                });
            }
        });

        // Plugin message Packet -> Trading
        protocol.registerOutgoing(State.PLAY, 0x18, 0x18, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Channel

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        if (wrapper.get(Type.STRING, 0).equalsIgnoreCase("MC|TrList")) {
                            wrapper.passthrough(Type.INT); // Passthrough Window ID

                            int size = wrapper.passthrough(Type.BYTE);
                            for (int i = 0; i < size; i++) {
                                wrapper.write(Type.ITEM, handleItemToClient(wrapper.read(Type.ITEM))); // Input Item
                                wrapper.write(Type.ITEM, handleItemToClient(wrapper.read(Type.ITEM))); // Output Item

                                boolean secondItem = wrapper.passthrough(Type.BOOLEAN); // Has second item
                                if (secondItem)
                                    wrapper.write(Type.ITEM, handleItemToClient(wrapper.read(Type.ITEM))); // Second Item

                                wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                                wrapper.passthrough(Type.INT); // Number of tools uses
                                wrapper.passthrough(Type.INT); // Maximum number of trade uses
                            }
                        }
                    }
                });
            }
        });

        // Click window packet
        protocol.registerIncoming(State.PLAY, 0x07, 0x07, new PacketRemapper() {
                    @Override
                    public void registerMap() {
                        map(Type.UNSIGNED_BYTE); // 0 - Window ID
                        map(Type.SHORT); // 1 - Slot
                        map(Type.BYTE); // 2 - Button
                        map(Type.SHORT); // 3 - Action number
                        map(Type.VAR_INT); // 4 - Mode
                        map(Type.ITEM); // 5 - Clicked Item

                        handler(new PacketHandler() {
                            @Override
                            public void handle(PacketWrapper wrapper) throws Exception {
                                Item item = wrapper.get(Type.ITEM, 0);
                                handleItemToServer(item);
                            }
                        });
                    }
                }
        );

        // Creative Inventory Action
        protocol.registerIncoming(State.PLAY, 0x18, 0x18, new PacketRemapper() {
                    @Override
                    public void registerMap() {
                        map(Type.SHORT); // 0 - Slot
                        map(Type.ITEM); // 1 - Clicked Item

                        handler(new PacketHandler() {
                            @Override
                            public void handle(PacketWrapper wrapper) throws Exception {
                                Item item = wrapper.get(Type.ITEM, 0);
                                handleItemToServer(item);
                            }
                        });
                    }
                }
        );

        /* Block packets */

        // Chunk packet
        protocol.registerOutgoing(State.PLAY, 0x20, 0x20, new PacketRemapper() {
                    @Override
                    public void registerMap() {
                        handler(new PacketHandler() {
                            @Override
                            public void handle(PacketWrapper wrapper) throws Exception {
                                ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);

                                Chunk1_9_3_4Type type = new Chunk1_9_3_4Type(clientWorld); // Use the 1.10 Chunk type since nothing changed.
                                Chunk1_9_3_4 chunk = (Chunk1_9_3_4) wrapper.passthrough(type);

                                handleChunk(chunk);
                            }
                        });
                    }
                }
        );

        // Block Change Packet
        protocol.registerOutgoing(State.PLAY, 0x0B, 0x0B, new PacketRemapper() {
                    @Override
                    public void registerMap() {
                        map(Type.POSITION); // 0 - Block Position
                        map(Type.VAR_INT); // 1 - Block

                        handler(new PacketHandler() {
                            @Override
                            public void handle(PacketWrapper wrapper) throws Exception {
                                int idx = wrapper.get(Type.VAR_INT, 0);
                                wrapper.set(Type.VAR_INT, 0, handleBlockID(idx));
                            }
                        });
                    }
                }
        );

        // Multi Block Change Packet
        protocol.registerOutgoing(State.PLAY, 0x10, 0x10, new PacketRemapper() {
                    @Override
                    public void registerMap() {
                        map(Type.INT); // 0 - Chunk X
                        map(Type.INT); // 1 - Chunk Z

                        handler(new PacketHandler() {
                            @Override
                            public void handle(PacketWrapper wrapper) throws Exception {
                                int count = wrapper.passthrough(Type.VAR_INT); // Array length

                                for (int i = 0; i < count; i++) {
                                    wrapper.passthrough(Type.UNSIGNED_BYTE); // Horizontal position
                                    wrapper.passthrough(Type.UNSIGNED_BYTE); // Y coords

                                    int id = wrapper.read(Type.VAR_INT); // Block ID
                                    wrapper.write(Type.VAR_INT, handleBlockID(id));
                                }
                            }
                        });
                    }
                }
        );

        // Update Block Entity
        protocol.registerOutgoing(State.PLAY, 0x09, 0x09, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Position
                map(Type.UNSIGNED_BYTE); // 1 - Action
                map(Type.NBT); // 2 - NBT

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // Remove on shulkerbox decleration TODO convert to normal chest to make it work correctly?
                        if (wrapper.get(Type.UNSIGNED_BYTE, 0) == 10) {
                            wrapper.cancel();
                        }
                        // Handler Spawners
                        if (wrapper.get(Type.UNSIGNED_BYTE, 0) == 1) {
                            CompoundTag tag = wrapper.get(Type.NBT, 0);
                            EntityTypeNames.toClientSpawner(tag);
                        }
                    }
                });
            }
        });

        protocol.getEntityPackets().registerMetaHandler().handle(e -> {
            Metadata data = e.getData();

            if (data.getMetaType().equals(MetaType1_9.Slot)) // Is Item
                data.setValue(handleItemToClient((Item) data.getValue()));

            return data;
        });
    }

    @Override
    protected void registerRewrites() {
        // ShulkerBoxes to chests
        for (int i = 219; i < 235; i++)
            rewrite(i)
                    .repItem(new Item((short) 54, (byte) 1, (short) 0, getNamedTag("1.11 Shulker Box (Color #" + (i - 219) + ")")))
                    .repBlock(new Block(54, 1))
                    .blockEntityHandler((block, tag) -> {
                        tag.remove("id");
                        tag.put(new StringTag("id", "minecraft:chest"));
                        return tag;
                    });

        // Observer to Dispenser TODO facing position?
        rewrite(218).repItem(new Item((short) 23, (byte) 1, (short) 0, getNamedTag("1.11 Observer"))).repBlock(new Block(23, 0));

        // Handle spawner block entity
        rewrite(52).blockEntityHandler((b, tag) -> {
            EntityTypeNames.toClientSpawner(tag);
            return tag;
        });

        // Rewrite spawn eggs TODO maybe intercept / handle server for creative instead of ViaBackwards NBT
        rewrite(383).itemHandler((i) -> {
            EntityTypeNames.toClientItem(i);
            return i;
        });

        // Totem of Undying to Dead Bush
        rewrite(449).repItem(new Item((short) 32, (byte) 1, (short) 0, getNamedTag("1.11 Totem of Undying")));

    }
}
