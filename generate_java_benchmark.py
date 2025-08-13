#!/usr/bin/env python3
import argparse
import os
from pathlib import Path

TEMPLATE_CLASS = """\
package {package};

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

{extra_decl}
public class {classname}{extends_clause}{implements_clause} {{

    // Some fields to give the compiler something to chew on
    private static final int CONST_{idx} = {idx};
    private int a{idx} = CONST_{idx};
    private String s{idx} = "{classname}";
    private List<Integer> list{idx} = new ArrayList<>();
{constructor}
    public int compute(int x) {{
        int sum = x + a{idx} + list{idx}.size();
        for (Integer v : list{idx}) sum += v;
        return sum;
    }}

    // Cross-reference to another generated class to force the compiler
    public int link({refclass} other) {{
        return other.compute(CONST_{idx}) + this.compute({idx});
    }}

    @Override
    public String toString() {{
        return s{idx} + "#" + a{idx} + ":" + list{idx};
    }}
}}
"""

TEMPLATE_INTERFACE = """\
package {package};

public interface {interfacename} {{
    int ifaceMethod(int x);
}}
"""

TEMPLATE_ABSTRACT = """\
package {package};

public abstract class {abstractname} {{
    protected int base;
    public {abstractname}(int base) {{ this.base = base; }}
    public int baseCompute(int x) {{ return x + base; }}
}}
"""

TEMPLATE_MAIN = """\
package {package};

public class Main {{
    public static void main(String[] args) {{
        int n = {count};
        int acc = 0;
        for (int i = 1; i <= n; i += Math.max(1, n/50)) {{
            try {{
                Class<?> c = Class.forName("{package}.Class" + String.format("%04d", i));
                Object o = c.getDeclaredConstructor().newInstance();
                acc ^= c.getMethod("compute", int.class).invoke(o, i).hashCode();
            }} catch (Throwable t) {{
                t.printStackTrace();
            }}
        }}
        System.out.println("OK acc=" + acc);
    }}
}}
"""

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", required=True, help="Output directory for generated Java sources")
    ap.add_argument("--count", type=int, default=1000, help="Number of classes to generate")
    ap.add_argument("--package", default="com.example.bench", help="Java package name")
    args = ap.parse_args()

    out_dir = Path(args.out)
    pkg_path = Path(*args.package.split("."))
    src_dir = out_dir / pkg_path
    os.makedirs(src_dir, exist_ok=True)

    # Create interface and abstract base
    (src_dir / "Iface0001.java").write_text(
        TEMPLATE_INTERFACE.format(package=args.package, interfacename="Iface0001"),
        encoding="utf-8"
    )
    (src_dir / "AbstractBase.java").write_text(
        TEMPLATE_ABSTRACT.format(package=args.package, abstractname="AbstractBase"),
        encoding="utf-8"
    )

    # Generate classes
    for i in range(1, args.count + 1):
        classname = f"Class{i:04d}"
        implements_clause = " implements Iface0001" if (i % 5 == 0) else ""
        extends_clause = " extends AbstractBase" if (i % 7 == 0) else ""
        extra_decl = "// Extends abstract base" if (i % 7 == 0) else ""

        # Reference target: previous class or wrap to last
        ref_idx = i - 1 if i > 1 else args.count
        refclass = f"Class{ref_idx:04d}"

        # Constructor handling
        if i % 7 == 0:
            constructor = f"""
            public {classname}() {{
                super({i});
                for (int j = 0; j < 3; j++) list{i}.add(j + CONST_{i});
            }}
        """
        else:
            constructor = f"""
            public {classname}() {{
                for (int j = 0; j < 3; j++) list{i}.add(j + CONST_{i});
            }}
        """

        body = TEMPLATE_CLASS.format(
            package=args.package,
            extra_decl=extra_decl,
            classname=classname,
            extends_clause=extends_clause,
            implements_clause=implements_clause,
            idx=i,
            constructor=constructor,
            refclass=refclass
        )

        # Add iface method if needed
        if i % 5 == 0:
            insertion_point = body.rfind("}")
            iface_impl = f"""

    @Override
    public int ifaceMethod(int x) {{
        return compute(x) ^ {i};
    }}
"""
            body = body[:insertion_point] + iface_impl + body[insertion_point:]

        (src_dir / f"{classname}.java").write_text(body, encoding="utf-8")

    # Main.java
    (src_dir / "Main.java").write_text(
        TEMPLATE_MAIN.format(package=args.package, count=args.count),
        encoding="utf-8"
    )

    print(f"Generated {args.count} classes in package {args.package} at {src_dir}")

if __name__ == "__main__":
    main()