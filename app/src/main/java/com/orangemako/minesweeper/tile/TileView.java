package com.orangemako.minesweeper.tile;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.view.View;
import android.widget.Toast;

import com.orangemako.minesweeper.R;
import com.orangemako.minesweeper.board.BoardSquare;
import com.orangemako.minesweeper.exceptions.InvalidArgumentException;
import com.orangemako.minesweeper.game.Game;
import com.orangemako.minesweeper.utilities.GraphicsUtils;

import java.util.HashMap;
import java.util.Map;

public class TileView extends View {
    // Board Square states
    public static final int COVERED = 0;
    public static final int FLAGGED_AS_MINE = 1;
    public static final int UNCOVERED = 2;

    private LevelListDrawable mDrawableContainer;
    private BoardSquare mBoardSquare;
    private Game mGame;

    static Map<Integer, Integer> sMineCountToColorMap = new HashMap<>();

    static {
        sMineCountToColorMap.put(1, Color.RED);
        sMineCountToColorMap.put(2, Color.BLUE);
        sMineCountToColorMap.put(3, Color.GREEN);
        sMineCountToColorMap.put(4, Color.DKGRAY);
        sMineCountToColorMap.put(5, Color.MAGENTA);
        sMineCountToColorMap.put(6, Color.CYAN);
        sMineCountToColorMap.put(7, Color.YELLOW);
        sMineCountToColorMap.put(8, Color.RED);
    }


    public TileView(Context context, Game game, int x, int y) throws InvalidArgumentException {
        super(context);

        mGame = game;
        mBoardSquare = game.getBoard().getBoardGrid()[y][x];

        setupBackgrounds();
        setupListeners();
    }

    private void setupListeners() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Toggle mine flag.  The drawable container level is equivalent to view state.
                switch (mDrawableContainer.getLevel()) {
                    case COVERED:
                        mDrawableContainer.setLevel(FLAGGED_AS_MINE);
                        break;
                    case FLAGGED_AS_MINE:
                        mDrawableContainer.setLevel(COVERED);
                        break;

                }
            }
        });

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Uncover tile.
                switch (mDrawableContainer.getLevel()) {
                    case COVERED:
                        mDrawableContainer.setLevel(UNCOVERED);
                        break;
                    case FLAGGED_AS_MINE:
                        String errorMessage = getContext().getResources().getString(R.string.uncover_tile_error);
                        Toast.makeText(view.getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        break;
                }

                // Return true to consume event.
                return true;
            }
        });
    }

    private void setupBackgrounds() throws InvalidArgumentException {
        Drawable coveredTile = setupCoveredTile();
        Drawable uncoveredTile = setupUncoveredTile();
        LayerDrawable flaggedMineDrawable = new LayerDrawable(new Drawable[]{coveredTile, new ConcentricCirclesDrawable()});

        mDrawableContainer = new LevelListDrawable();
        mDrawableContainer.addLevel(0, COVERED, coveredTile);
        mDrawableContainer.addLevel(0, FLAGGED_AS_MINE, flaggedMineDrawable);
        mDrawableContainer.addLevel(0, UNCOVERED, uncoveredTile);

        setBackground(mDrawableContainer);
    }

    private Drawable setupCoveredTile() throws InvalidArgumentException {
        // TODO: Move this to a theme
        int colorInner = GraphicsUtils.getColor(getContext(), R.color.blue_grey_200);
        int colorTop = GraphicsUtils.getColor(getContext(), R.color.blue_grey_300);
        int colorLeft = GraphicsUtils.getColor(getContext(), R.color.blue_grey_400);
        int colorBottom = GraphicsUtils.getColor(getContext(), R.color.blue_grey_500);
        int colorRight = GraphicsUtils.getColor(getContext(), R.color.blue_grey_600);

        int[] tileColors = new int[]{colorInner, colorLeft, colorTop, colorRight, colorBottom};

        return new BeveledTileDrawable(tileColors);
    }

    private Drawable setupUncoveredTile() {
        Drawable uncoveredDrawable;

        if(mBoardSquare != null && mBoardSquare.doesContainMine()) {
            uncoveredDrawable = new ConcentricCirclesDrawable(new int[]{Color.RED, Color.BLACK}, 0.50f);
        }
        else {
            String adjacentMineCountText = "";
            int textColor = 0;

            if(mBoardSquare != null) {
                int adjacentMinesCount = mBoardSquare.getAdjacentMinesCount();

                if(adjacentMinesCount > 0) {
                    textColor = sMineCountToColorMap.get(adjacentMinesCount);
                    adjacentMineCountText = String.valueOf(adjacentMinesCount);
                }
            }

            uncoveredDrawable = new TextDrawable(adjacentMineCountText, textColor);
        }
        return uncoveredDrawable;
    }
}
