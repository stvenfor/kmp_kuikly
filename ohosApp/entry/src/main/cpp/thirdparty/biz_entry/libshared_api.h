#ifndef KONAN_LIBSHARED_H
#define KONAN_LIBSHARED_H
#ifdef __cplusplus
extern "C" {
#endif
#ifdef __cplusplus
typedef bool            libshared_KBoolean;
#else
typedef _Bool           libshared_KBoolean;
#endif
typedef unsigned short     libshared_KChar;
typedef signed char        libshared_KByte;
typedef short              libshared_KShort;
typedef int                libshared_KInt;
typedef long long          libshared_KLong;
typedef unsigned char      libshared_KUByte;
typedef unsigned short     libshared_KUShort;
typedef unsigned int       libshared_KUInt;
typedef unsigned long long libshared_KULong;
typedef float              libshared_KFloat;
typedef double             libshared_KDouble;
typedef float __attribute__ ((__vector_size__ (16))) libshared_KVector128;
typedef void*              libshared_KNativePtr;
struct libshared_KType;
typedef struct libshared_KType libshared_KType;

typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Byte;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Short;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Int;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Long;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Float;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Double;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Char;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Boolean;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_Unit;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_UByte;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_UShort;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_UInt;
typedef struct {
  libshared_KNativePtr pinned;
} libshared_kref_kotlin_ULong;


typedef struct {
  /* Service functions. */
  void (*DisposeStablePointer)(libshared_KNativePtr ptr);
  void (*DisposeString)(const char* string);
  libshared_KBoolean (*IsInstance)(libshared_KNativePtr ref, const libshared_KType* type);
  libshared_kref_kotlin_Byte (*createNullableByte)(libshared_KByte);
  libshared_KByte (*getNonNullValueOfByte)(libshared_kref_kotlin_Byte);
  libshared_kref_kotlin_Short (*createNullableShort)(libshared_KShort);
  libshared_KShort (*getNonNullValueOfShort)(libshared_kref_kotlin_Short);
  libshared_kref_kotlin_Int (*createNullableInt)(libshared_KInt);
  libshared_KInt (*getNonNullValueOfInt)(libshared_kref_kotlin_Int);
  libshared_kref_kotlin_Long (*createNullableLong)(libshared_KLong);
  libshared_KLong (*getNonNullValueOfLong)(libshared_kref_kotlin_Long);
  libshared_kref_kotlin_Float (*createNullableFloat)(libshared_KFloat);
  libshared_KFloat (*getNonNullValueOfFloat)(libshared_kref_kotlin_Float);
  libshared_kref_kotlin_Double (*createNullableDouble)(libshared_KDouble);
  libshared_KDouble (*getNonNullValueOfDouble)(libshared_kref_kotlin_Double);
  libshared_kref_kotlin_Char (*createNullableChar)(libshared_KChar);
  libshared_KChar (*getNonNullValueOfChar)(libshared_kref_kotlin_Char);
  libshared_kref_kotlin_Boolean (*createNullableBoolean)(libshared_KBoolean);
  libshared_KBoolean (*getNonNullValueOfBoolean)(libshared_kref_kotlin_Boolean);
  libshared_kref_kotlin_Unit (*createNullableUnit)(void);
  libshared_kref_kotlin_UByte (*createNullableUByte)(libshared_KUByte);
  libshared_KUByte (*getNonNullValueOfUByte)(libshared_kref_kotlin_UByte);
  libshared_kref_kotlin_UShort (*createNullableUShort)(libshared_KUShort);
  libshared_KUShort (*getNonNullValueOfUShort)(libshared_kref_kotlin_UShort);
  libshared_kref_kotlin_UInt (*createNullableUInt)(libshared_KUInt);
  libshared_KUInt (*getNonNullValueOfUInt)(libshared_kref_kotlin_UInt);
  libshared_kref_kotlin_ULong (*createNullableULong)(libshared_KULong);
  libshared_KULong (*getNonNullValueOfULong)(libshared_kref_kotlin_ULong);

  /* User functions. */
  struct {
    struct {
      struct {
        struct {
          struct {
            struct {
              libshared_KInt (*com_example_kuikly_pages_ComposeAnimPage$stableprop_getter)();
              libshared_KInt (*com_example_kuikly_pages_ComposeListPage$stableprop_getter)();
              libshared_KInt (*com_example_kuikly_pages_ComposePagerPage$stableprop_getter)();
              libshared_KInt (*com_example_kuikly_pages_DslLabPage$stableprop_getter)();
              libshared_KInt (*com_example_kuikly_pages_GalleryPage$stableprop_getter)();
              libshared_KInt (*com_example_kuikly_pages_HomePage$stableprop_getter)();
              libshared_KInt (*com_example_kuikly_pages_PerfLabPage$stableprop_getter)();
              libshared_KInt (*com_example_kuikly_pages_PermissionDemoPage$stableprop_getter)();
              libshared_KInt (*com_example_kuikly_pages_ShareDemoPage$stableprop_getter)();
              libshared_KInt (*com_example_kuikly_pages_SplashPage$stableprop_getter)();
              libshared_KInt (*com_example_kuikly_pages_ComposeAnimPage$stableprop_getter_)();
              libshared_KInt (*com_example_kuikly_pages_ComposeListPage$stableprop_getter_)();
              libshared_KInt (*com_example_kuikly_pages_ComposePagerPage$stableprop_getter_)();
              libshared_KInt (*com_example_kuikly_pages_DslLabPage$stableprop_getter_)();
              libshared_KInt (*com_example_kuikly_pages_GalleryPage$stableprop_getter_)();
              libshared_KInt (*com_example_kuikly_pages_HomePage$stableprop_getter_)();
              libshared_KInt (*com_example_kuikly_pages_PerfLabPage$stableprop_getter_)();
              libshared_KInt (*com_example_kuikly_pages_PermissionDemoPage$stableprop_getter_)();
              libshared_KInt (*com_example_kuikly_pages_ShareDemoPage$stableprop_getter_)();
              libshared_KInt (*com_example_kuikly_pages_SplashPage$stableprop_getter_)();
              libshared_KInt (*com_example_kuikly_pages_ComposeAnimPage$stableprop_getter__)();
              libshared_KInt (*com_example_kuikly_pages_ComposeListPage$stableprop_getter__)();
              libshared_KInt (*com_example_kuikly_pages_ComposePagerPage$stableprop_getter__)();
              libshared_KInt (*com_example_kuikly_pages_DslLabPage$stableprop_getter__)();
              libshared_KInt (*com_example_kuikly_pages_GalleryPage$stableprop_getter__)();
              libshared_KInt (*com_example_kuikly_pages_HomePage$stableprop_getter__)();
              libshared_KInt (*com_example_kuikly_pages_PerfLabPage$stableprop_getter__)();
              libshared_KInt (*com_example_kuikly_pages_PermissionDemoPage$stableprop_getter__)();
              libshared_KInt (*com_example_kuikly_pages_ShareDemoPage$stableprop_getter__)();
              libshared_KInt (*com_example_kuikly_pages_SplashPage$stableprop_getter__)();
              libshared_KInt (*com_example_kuikly_pages_ComposeAnimPage$stableprop_getter___)();
              libshared_KInt (*com_example_kuikly_pages_ComposeListPage$stableprop_getter___)();
              libshared_KInt (*com_example_kuikly_pages_ComposePagerPage$stableprop_getter___)();
              libshared_KInt (*com_example_kuikly_pages_DslLabPage$stableprop_getter___)();
              libshared_KInt (*com_example_kuikly_pages_GalleryPage$stableprop_getter___)();
              libshared_KInt (*com_example_kuikly_pages_HomePage$stableprop_getter___)();
              libshared_KInt (*com_example_kuikly_pages_PerfLabPage$stableprop_getter___)();
              libshared_KInt (*com_example_kuikly_pages_PermissionDemoPage$stableprop_getter___)();
              libshared_KInt (*com_example_kuikly_pages_ShareDemoPage$stableprop_getter___)();
              libshared_KInt (*com_example_kuikly_pages_SplashPage$stableprop_getter___)();
              libshared_KInt (*com_example_kuikly_pages_ComposeAnimPage$stableprop_getter____)();
              libshared_KInt (*com_example_kuikly_pages_ComposeListPage$stableprop_getter____)();
              libshared_KInt (*com_example_kuikly_pages_ComposePagerPage$stableprop_getter____)();
              libshared_KInt (*com_example_kuikly_pages_DslLabPage$stableprop_getter____)();
              libshared_KInt (*com_example_kuikly_pages_GalleryPage$stableprop_getter____)();
              libshared_KInt (*com_example_kuikly_pages_HomePage$stableprop_getter____)();
              libshared_KInt (*com_example_kuikly_pages_PerfLabPage$stableprop_getter____)();
              libshared_KInt (*com_example_kuikly_pages_PermissionDemoPage$stableprop_getter____)();
              libshared_KInt (*com_example_kuikly_pages_ShareDemoPage$stableprop_getter____)();
              libshared_KInt (*com_example_kuikly_pages_SplashPage$stableprop_getter____)();
              libshared_KInt (*com_example_kuikly_pages_ComposeAnimPage$stableprop_getter_____)();
              libshared_KInt (*com_example_kuikly_pages_ComposeListPage$stableprop_getter_____)();
              libshared_KInt (*com_example_kuikly_pages_ComposePagerPage$stableprop_getter_____)();
              libshared_KInt (*com_example_kuikly_pages_DslLabPage$stableprop_getter_____)();
              libshared_KInt (*com_example_kuikly_pages_GalleryPage$stableprop_getter_____)();
              libshared_KInt (*com_example_kuikly_pages_HomePage$stableprop_getter_____)();
              libshared_KInt (*com_example_kuikly_pages_PerfLabPage$stableprop_getter_____)();
              libshared_KInt (*com_example_kuikly_pages_PermissionDemoPage$stableprop_getter_____)();
              libshared_KInt (*com_example_kuikly_pages_ShareDemoPage$stableprop_getter_____)();
              libshared_KInt (*com_example_kuikly_pages_SplashPage$stableprop_getter_____)();
              libshared_KInt (*com_example_kuikly_pages_ComposeAnimPage$stableprop_getter______)();
              libshared_KInt (*com_example_kuikly_pages_ComposeListPage$stableprop_getter______)();
              libshared_KInt (*com_example_kuikly_pages_ComposePagerPage$stableprop_getter______)();
              libshared_KInt (*com_example_kuikly_pages_DslLabPage$stableprop_getter______)();
              libshared_KInt (*com_example_kuikly_pages_GalleryPage$stableprop_getter______)();
              libshared_KInt (*com_example_kuikly_pages_HomePage$stableprop_getter______)();
              libshared_KInt (*com_example_kuikly_pages_PerfLabPage$stableprop_getter______)();
              libshared_KInt (*com_example_kuikly_pages_PermissionDemoPage$stableprop_getter______)();
              libshared_KInt (*com_example_kuikly_pages_ShareDemoPage$stableprop_getter______)();
              libshared_KInt (*com_example_kuikly_pages_SplashPage$stableprop_getter______)();
              libshared_KInt (*com_example_kuikly_pages_ComposeAnimPage$stableprop_getter_______)();
              libshared_KInt (*com_example_kuikly_pages_ComposeListPage$stableprop_getter_______)();
              libshared_KInt (*com_example_kuikly_pages_ComposePagerPage$stableprop_getter_______)();
              libshared_KInt (*com_example_kuikly_pages_DslLabPage$stableprop_getter_______)();
              libshared_KInt (*com_example_kuikly_pages_GalleryPage$stableprop_getter_______)();
              libshared_KInt (*com_example_kuikly_pages_HomePage$stableprop_getter_______)();
              libshared_KInt (*com_example_kuikly_pages_PerfLabPage$stableprop_getter_______)();
              libshared_KInt (*com_example_kuikly_pages_PermissionDemoPage$stableprop_getter_______)();
              libshared_KInt (*com_example_kuikly_pages_ShareDemoPage$stableprop_getter_______)();
              libshared_KInt (*com_example_kuikly_pages_SplashPage$stableprop_getter_______)();
              libshared_KInt (*com_example_kuikly_pages_ComposeAnimPage$stableprop_getter________)();
              libshared_KInt (*com_example_kuikly_pages_ComposeListPage$stableprop_getter________)();
              libshared_KInt (*com_example_kuikly_pages_ComposePagerPage$stableprop_getter________)();
              libshared_KInt (*com_example_kuikly_pages_DslLabPage$stableprop_getter________)();
              libshared_KInt (*com_example_kuikly_pages_GalleryPage$stableprop_getter________)();
              libshared_KInt (*com_example_kuikly_pages_HomePage$stableprop_getter________)();
              libshared_KInt (*com_example_kuikly_pages_PerfLabPage$stableprop_getter________)();
              libshared_KInt (*com_example_kuikly_pages_PermissionDemoPage$stableprop_getter________)();
              libshared_KInt (*com_example_kuikly_pages_ShareDemoPage$stableprop_getter________)();
              libshared_KInt (*com_example_kuikly_pages_SplashPage$stableprop_getter________)();
              libshared_KInt (*com_example_kuikly_pages_ComposeAnimPage$stableprop_getter_________)();
              libshared_KInt (*com_example_kuikly_pages_ComposeListPage$stableprop_getter_________)();
              libshared_KInt (*com_example_kuikly_pages_ComposePagerPage$stableprop_getter_________)();
              libshared_KInt (*com_example_kuikly_pages_DslLabPage$stableprop_getter_________)();
              libshared_KInt (*com_example_kuikly_pages_GalleryPage$stableprop_getter_________)();
              libshared_KInt (*com_example_kuikly_pages_HomePage$stableprop_getter_________)();
              libshared_KInt (*com_example_kuikly_pages_PerfLabPage$stableprop_getter_________)();
              libshared_KInt (*com_example_kuikly_pages_PermissionDemoPage$stableprop_getter_________)();
              libshared_KInt (*com_example_kuikly_pages_ShareDemoPage$stableprop_getter_________)();
              libshared_KInt (*com_example_kuikly_pages_SplashPage$stableprop_getter_________)();
            } pages;
            libshared_KInt (*com_example_kuikly_HelloWorldPage$stableprop_getter)();
          } kuikly;
        } example;
      } com;
      libshared_KInt (*initKuikly)();
    } root;
  } kotlin;
} libshared_ExportedSymbols;
extern libshared_ExportedSymbols* libshared_symbols(void);
#ifdef __cplusplus
}  /* extern "C" */
#endif
#endif  /* KONAN_LIBSHARED_H */
