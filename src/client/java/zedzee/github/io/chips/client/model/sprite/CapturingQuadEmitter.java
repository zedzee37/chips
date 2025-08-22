package zedzee.github.io.chips.client.model.sprite;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// this probably shouldnt be done, but i think i have to?
public class CapturingQuadEmitter implements QuadEmitter {
    private final Map<Direction, List<ChipsSprite>> quadMap = new HashMap<>();

    private ChipsSprite.Builder spriteBuilder = new ChipsSprite.Builder();
    private Direction currentDirection = Direction.NORTH;

    public CapturingQuadEmitter() {
        initBuilder();
    }

    private void initBuilder() {
        this.spriteBuilder = new ChipsSprite.Builder();
        this.currentDirection = Direction.NORTH;
    }

    @Override
    public QuadEmitter pos(int vertexIndex, float x, float y, float z) {
        return null;
    }

    @Override
    public QuadEmitter color(int vertexIndex, int color) {
        return null;
    }

    @Override
    public QuadEmitter uv(int vertexIndex, float u, float v) {
        return null;
    }

    @Override
    public QuadEmitter lightmap(int vertexIndex, int lightmap) {
        return null;
    }

    @Override
    public QuadEmitter normal(int vertexIndex, float x, float y, float z) {
        return null;
    }

    @Override
    public QuadEmitter nominalFace(@Nullable Direction face) {
        this.currentDirection = face;
        return this;
    }

    @Override
    public QuadEmitter cullFace(@Nullable Direction face) {
        return null;
    }

    @Override
    public QuadEmitter renderLayer(@Nullable BlockRenderLayer renderLayer) {
        return null;
    }

    @Override
    public QuadEmitter emissive(boolean emissive) {
        return null;
    }

    @Override
    public QuadEmitter diffuseShade(boolean shade) {
        return null;
    }

    @Override
    public QuadEmitter ambientOcclusion(TriState ao) {
        return null;
    }

    @Override
    public QuadEmitter glint(ItemRenderState.@Nullable Glint glint) {
        return null;
    }

    @Override
    public QuadEmitter shadeMode(ShadeMode mode) {
        return null;
    }

    @Override
    public QuadEmitter tintIndex(int tintIndex) {
        return null;
    }

    @Override
    public QuadEmitter tag(int tag) {
        return null;
    }

    @Override
    public QuadEmitter copyFrom(QuadView quad) {
        return null;
    }

    @Override
    public QuadEmitter fromVanilla(int[] vertexData, int startIndex) {
        return null;
    }

    @Override
    public QuadEmitter fromBakedQuad(BakedQuad quad) {
        return null;
    }

    @Override
    public void pushTransform(QuadTransform transform) {

    }

    @Override
    public void popTransform() {

    }

    @Override
    public QuadEmitter emit() {
        buildSprite();
        initBuilder();
        return this;
    }

    private void buildSprite() {
        quadMap.putIfAbsent(currentDirection, new ArrayList<>());
        List<ChipsSprite> spriteList = quadMap.get(currentDirection);
        spriteList.add(spriteBuilder.build());
    }

    @Override
    public float x(int vertexIndex) {
        return 0;
    }

    @Override
    public float y(int vertexIndex) {
        return 0;
    }

    @Override
    public float z(int vertexIndex) {
        return 0;
    }

    @Override
    public float posByIndex(int vertexIndex, int coordinateIndex) {
        return 0;
    }

    @Override
    public Vector3f copyPos(int vertexIndex, @Nullable Vector3f target) {
        return null;
    }

    @Override
    public int color(int vertexIndex) {
        return 0;
    }

    @Override
    public float u(int vertexIndex) {
        return 0;
    }

    @Override
    public float v(int vertexIndex) {
        return 0;
    }

    @Override
    public Vector2f copyUv(int vertexIndex, @Nullable Vector2f target) {
        return null;
    }

    @Override
    public int lightmap(int vertexIndex) {
        return 0;
    }

    @Override
    public boolean hasNormal(int vertexIndex) {
        return false;
    }

    @Override
    public float normalX(int vertexIndex) {
        return 0;
    }

    @Override
    public float normalY(int vertexIndex) {
        return 0;
    }

    @Override
    public float normalZ(int vertexIndex) {
        return 0;
    }

    @Override
    public @Nullable Vector3f copyNormal(int vertexIndex, @Nullable Vector3f target) {
        return null;
    }

    @Override
    public Vector3fc faceNormal() {
        return null;
    }

    @Override
    public @NotNull Direction lightFace() {
        return null;
    }

    @Override
    public @Nullable Direction nominalFace() {
        return null;
    }

    @Override
    public @Nullable Direction cullFace() {
        return null;
    }

    @Override
    public @Nullable BlockRenderLayer renderLayer() {
        return null;
    }

    @Override
    public boolean emissive() {
        return false;
    }

    @Override
    public boolean diffuseShade() {
        return false;
    }

    @Override
    public TriState ambientOcclusion() {
        return null;
    }

    @Override
    public ItemRenderState.@Nullable Glint glint() {
        return null;
    }

    @Override
    public ShadeMode shadeMode() {
        return null;
    }

    @Override
    public int tintIndex() {
        return 0;
    }

    @Override
    public int tag() {
        return 0;
    }

    @Override
    public void toVanilla(int[] target, int startIndex) {

    }

    @Override
    public QuadEmitter spriteBake(Sprite sprite, int bakeFlags) {
        spriteBuilder.sprite(sprite);
        return this;
    }
}
