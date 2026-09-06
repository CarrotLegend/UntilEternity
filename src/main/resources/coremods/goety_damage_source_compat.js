var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

function initializeCoreMod() {
    return {
        'goety_mod_damage_source_compat': {
            'target': {
                'type': 'CLASS',
                'name': 'com.Polarice3.Goety.utils.ModDamageSource'
            },

            'transformer': function(classNode) {
                var owner =
                    'com/Polarice3/Goety/utils/ModDamageSource';
                var newDescriptor =
                    '(Lnet/minecraft/world/level/Level;' +
                    'Lnet/minecraft/resources/ResourceKey;)' +
                    'Lnet/minecraft/world/damagesource/DamageSource;';
                var oldDescriptor =
                    '(Lnet/minecraft/world/level/Level;' +
                    'Lnet/minecraft/resources/ResourceKey;' +
                    '[Lnet/minecraft/world/entity/EntityType;)' +
                    'Lnet/minecraft/world/damagesource/DamageSource;';
                for (var i = 0; i < classNode.methods.size(); i++) {
                    var existing = classNode.methods.get(i);

                    if (existing.name === 'getDamageSource'
                        && existing.desc === newDescriptor) {

                        ASMAPI.log(
                            'INFO',
                            '[Until Eternity] Goety already contains ' +
                            'getDamageSource(Level, ResourceKey), skipping compat patch.'
                        );

                        return classNode;
                    }
                }
                var method = ASMAPI.getMethodNode();

                method.access =
                    Opcodes.ACC_PUBLIC |
                    Opcodes.ACC_STATIC;

                method.name = 'getDamageSource';
                method.desc = newDescriptor;
                method.instructions.add(
                    new VarInsnNode(
                        Opcodes.ALOAD,
                        0
                    )
                );
                method.instructions.add(
                    new VarInsnNode(
                        Opcodes.ALOAD,
                        1
                    )
                );
                method.instructions.add(
                    new InsnNode(
                        Opcodes.ICONST_0
                    )
                );
                method.instructions.add(
                    new TypeInsnNode(
                        Opcodes.ANEWARRAY,
                        'net/minecraft/world/entity/EntityType'
                    )
                );

                method.instructions.add(
                    new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        owner,
                        'getDamageSource',
                        oldDescriptor,
                        false
                    )
                );

                method.instructions.add(
                    new InsnNode(
                        Opcodes.ARETURN
                    )
                );

                method.maxStack = 3;
                method.maxLocals = 2;

                classNode.methods.add(method);

                ASMAPI.log(
                    'INFO',
                    '[Until Eternity] Added Goety 2.5.53+ ' +
                    'getDamageSource(Level, ResourceKey) compatibility method.'
                );

                return classNode;
            }
        }
    };
}