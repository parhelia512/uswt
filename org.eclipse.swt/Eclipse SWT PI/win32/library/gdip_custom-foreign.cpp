#include "windows.h"
#include "gdiplus.h"
#include "stdint_compat.h"
using namespace Gdiplus;

#ifndef NO_Graphics_1DrawLines
extern "C" int32_t
CNIProxy_Graphics_DrawLines_i_i_ia_i
  (int32_t p0, int32_t p1, int32_t* p2, int32_t p3)
{
  int32_t rc = 0;
  if (p2) {
    Point* points = new Point[p3];
    if (points == 0) return 0;
    for (int i = 0, j = 0; i < p3; ++i, j += 2) {
      points[i] = Point(p2[j], p2[j + 1]);
    }
    rc = (int32_t) ((Graphics*) p0)->DrawLines
      ((Pen*) p1, points, (INT) p3);
    delete[] points;
  }
  return rc;
}
#endif

#ifndef NO_Graphics_1DrawPolygon
extern "C" int32_t
CNIProxy_Graphics_DrawPolygon_i_i_ia_i
  (int32_t p0, int32_t p1, int32_t* p2, int32_t p3)
{
  int32_t rc = 0;
  if (p2) {
    Point* points = new Point[p3];
    if (points == 0) return 0;
    for (int i = 0, j = 0; i < p3; ++i, j += 2) {
      points[i] = Point(p2[j], p2[j + 1]);
    }
    rc = (int32_t) ((Graphics*) p0)->DrawPolygon
      ((Pen*) p1, points, (INT) p3);
    delete[] points;
  }
  return 0;
}
#endif

#ifndef NO_Graphics_1FillPolygon
extern "C" int32_t
CNIProxy_Graphics_FillPolygon_i_i_ia_i_i
(int32_t p0, int32_t p1, int32_t* p2, int32_t p3, int32_t p4)
{
  int32_t rc = 0;
  if (p2) {
    Point* points = new Point[p3];
    if (points == 0) return 0;
    for (int i = 0, j = 0; i < p3; ++i, j += 2) {
      points[i] = Point(p2[j], p2[j + 1]);
    }
    rc = (int32_t) ((Graphics*) p0)->FillPolygon
      ((Brush*) p1, points, (INT) p3, (FillMode) p4);
    delete[] points;
  }
  return rc;
}
#endif

#ifndef NO_GraphicsPath_1GetPathPoints
extern "C" int32_t
CNIProxy_GraphicsPath_GetPathPoints_i_fa_i
  (int32_t p0, float* p1, int32_t p2)
{
  int32_t rc = 0;
  if (p1) {
    Point* points = new Point[p2];
    if (points == 0) return 0;
    rc = (int32_t) ((GraphicsPath*) p0)->GetPathPoints(points, p2);
    for (int i = 0, j = 0; i < p2; ++i, j += 2) {
      p1[j] = points[i].X;
      p1[j + 1] = points[i].Y;
    }
    delete[] points;
  }
  return rc;
}
#endif

#ifndef NO_Matrix_1TransformPoints__I_3FI
extern "C" int32_t
CNIProxy_Matrix_TransformPoints_i_fa_i
  (int32_t p0, float* p1, int32_t p2)
{
  int32_t rc = 0;
  if (p1) {
    PointF* points = new PointF[p2];
    if (points == 0) return 0;
    for (int i = 0, j = 0; i < p2; ++i, j += 2) {
      points[i] = PointF(p1[j], p1[j + 1]);
    }
    rc = (int32_t) ((Matrix*) p0)->TransformPoints(points, p2);
    for (int i = 0, j = 0; i < p2; ++i, j += 2) {
      p1[j] = points[i].X;
      p1[j + 1] = points[i].Y;
    }
    delete[] points;
  }
  return 0;
}
#endif

#ifndef NO_LinearGradientBrush_1SetInterpolationColors
extern "C" int32_t
CNIProxy_LinearGradientBrush_SetInterpolationColors_i_ia_fa_i
  (int32_t p0, int32_t* p1, float* p2, int32_t p3)
{
  int32_t rc = 0;
  if (p1) {
    Color* colors = new Color[p3];
    if (colors == 0) return 0;
    for (int i = 0; i < p3; ++i) {
      colors[i] = *(Color*) p1[i];
    }
    rc = (int32_t) ((LinearGradientBrush*) p0)->SetInterpolationColors
      (colors, (const REAL*) p2, p3);
    delete[] colors;
  }
  return rc;
}
#endif

#ifndef NO_PathGradientBrush_1SetSurroundColors
extern "C" int32_t
CNIProxy_PathGradientBrush_SetSurroundColors_i_ia_ia
  (int32_t p0, int32_t* p1, int32_t* p2)
{
  int32_t rc = 0;
  if (p1) {
    Color* colors = new Color[p2[0]];
    if (colors == 0) return 0;
    for (int i = 0; i < p2[0]; ++i) {
      colors[i] = *(Color*) p1[i];
    }
    rc = (int32_t) ((PathGradientBrush*) p0)->SetSurroundColors
      (colors, (INT*) p2);
    delete[] colors;
  }
  return rc;
}
#endif

#ifndef NO_GraphicsPath_1new___3I_3BII
extern "C" int32_t
CNIProxy_GraphicsPath_new_ia_ba_i_i
  (int32_t* p0, int8_t* p1, int32_t p2, int32_t p3)
{
  int32_t rc = 0;
  if (p1) {
    PointF* points = new PointF[p2];
    if (points == 0) return 0;
    for (int i = 0, j = 0; i < p2; ++i, j += 2) {
      points[i] = PointF(p1[j], p1[j + 1]);
    }
    rc = (int32_t) new GraphicsPath(points, (BYTE*) p1, p2, (FillMode) p3);
    delete[] points;
  } else {
    rc = (int32_t) new GraphicsPath
      ((PointF*) 0, (BYTE*) p1, p2, (FillMode) p3);
  }
  return rc;
}
#endif
